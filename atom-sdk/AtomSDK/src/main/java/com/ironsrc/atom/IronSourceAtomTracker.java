package com.ironsrc.atom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ironSource Atom high level API class, support track() and flush()
 */
public class IronSourceAtomTracker {
    private static String TAG_ = "IronSourceAtomTracker";

    private static int TASK_WORKER_COUNT_ = 24;
    private static int TASK_POOL_SIZE_ = 10000;

    /**
     * The flush interval in milliseconds
     */
    private long flushInterval_ = 1000;

    private int bulkSize_ = 500;

    /**
     * The size of the bulk in bytes.
     */
    private int bulkBytesSize_ = 64 * 1024;

    // Jitter time
    private double minTime_ = 1;
    private double maxTime_ = 10;

    private IronSourceAtom api_;

    private Boolean isDebug_ = false;
    private Boolean isFlushData_ = false;


    private Boolean isRunWorker_ = true;
    private Thread eventWorkerThread_;

    private ConcurrentHashMap<String, String> streamData_;

    private IEventManager eventManager_;
    private EventTaskPool eventPool_;
    private Random random_;

    /**
     * Atom Tracker constructor
     */
    public IronSourceAtomTracker() {
        this(TASK_WORKER_COUNT_, TASK_POOL_SIZE_);
    }

    /**
     * Atom Tracker constructor
     *
     * @param taskWorkersCount amount of workers (threads) for concurrent sending
     * @param taskPoolSize     amount of bulk events ({stream,auth,buffer}) to store in task pool
     */
    public IronSourceAtomTracker(int taskWorkersCount, int taskPoolSize) {
        api_ = new IronSourceAtom();
        eventPool_ = new EventTaskPool(taskWorkersCount, taskPoolSize);

        eventManager_ = new QueueEventManager();
        streamData_ = new ConcurrentHashMap<String, String>();

        random_ = new Random();

        eventWorkerThread_ = new Thread(new Runnable() {
            public void run() {
                eventWorker();
            }
        });
        eventWorkerThread_.start();
    }

    /**
     * Stop all event pool threads
     */
    public void stop() {
        isRunWorker_ = false;
        eventPool_.stop();
    }

    /**
     * Sets the event manager.
     *
     * @param eventManager custom event manager
     */
    public void setEventManager(IEventManager eventManager) {
        eventManager_ = eventManager;
    }

    /**
     * Enabling print debug information
     *
     * @param isDebug If set to true is debug.
     */
    public void enableDebug(Boolean isDebug) {
        isDebug_ = isDebug;

        api_.enableDebug(isDebug);
    }

    /**
     * Set Auth Key for stream
     *
     * @param authKey HMAC auth key for stream
     */
    public void setAuth(String authKey) {
        api_.setAuth(authKey);
    }

    /**
     * Set Atom Endpoint for sending data
     *
     * @param endpoint for address of server
     */
    public void setEndpoint(String endpoint) {
        api_.setEndpoint(endpoint);
    }

    /**
     * Set bulk size (amount of events) for flush
     *
     * @param bulkSize upon reaching this amount, flush the buffer
     */
    public void setBulkSize(int bulkSize) {
        bulkSize_ = bulkSize;
    }

    /**
     * Set bulk size in bytes
     *
     * @param bulkBytesSize upon reaching this size, flush the buffer
     */
    public void setBulkBytesSize(int bulkBytesSize) {
        bulkBytesSize_ = bulkBytesSize;
    }

    /**
     * Set data flushing interval
     *
     * @param flushInterval flush the events every {flush interval} ms
     */
    public void setFlushInterval(long flushInterval) {
        flushInterval_ = flushInterval;
    }

    /**
     * Track data (store it before sending)
     *
     * @param stream  stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     */
    public void track(String stream, String data, String authKey) {
        if (authKey.length() == 0) {
            authKey = api_.getAuth();
        }

        if (!streamData_.containsKey(stream)) {
            streamData_.putIfAbsent(stream, authKey);
        }
        eventManager_.addEvent(new Event(stream, data, authKey));
    }

    /**
     * Track data (store it before sending)
     *
     * @param stream stream name for saving data in db table
     * @param data   user data to send
     */
    public void track(String stream, String data) {
        this.track(stream, data, api_.getAuth());
    }

    /**
     * Flush all data to Atom API
     */
    public void flush() {
        isFlushData_ = true;
    }

    /**
     * Gets the duration for calculating retry time on failure
     *
     * @param attempt attempt count
     * @return duration
     */
    private double getRetryTime(int attempt) {
        double duration = minTime_ * Math.pow(2, attempt);
        duration = (random_.nextDouble() * (duration - minTime_)) + minTime_;

        if (duration > maxTime_) {
            duration = maxTime_;
        }

        return duration;
    }

    /**
     * Flush event to stream
     *
     * @param stream  stream name for saving data in db table
     * @param authKey HMAC auth key for stream
     * @param events  list of events
     */
    private void flushEvent(String stream, String authKey, LinkedList<String> events) {
        List<String> buffer = new LinkedList<String>(events);
        events.clear();

        try {
            eventPool_.addEvent(new EventTask(stream, authKey, buffer) {
                public void action() {
                    flushData(this.stream_, this.authKey_, this.buffer_);
                }
            });
        } catch (Exception ex) {
            printLog(ex.getMessage());
        }
    }

    /**
     * Main tracker handler function, handles the flushing conditions
     */
    private void eventWorker() {
        HashMap<String, Long> timerStartTime = new HashMap<String, Long>();
        HashMap<String, Long> timerDeltaTime = new HashMap<String, Long>();

        // temporary buffers for hold event data per stream
        HashMap<String, LinkedList<String>> eventsBuffer = new HashMap<String, LinkedList<String>>();
        // buffers size storage
        HashMap<String, Integer> eventsSize = new HashMap<String, Integer>();

        Boolean isClearSize = false;

        while (isRunWorker_) {
            for (Map.Entry<String, String> entry : streamData_.entrySet()) {
                String streamName = entry.getKey();
                if (!timerStartTime.containsKey(streamName)) {
                    timerStartTime.put(streamName, Utils.getCurrentMilliseconds());
                }

                if (!timerDeltaTime.containsKey(streamName)) {
                    timerDeltaTime.put(streamName, 0L);
                }

                timerDeltaTime.put(streamName, timerDeltaTime.get(streamName) +
                        (Utils.getCurrentMilliseconds() - timerStartTime.get(streamName)));
                timerStartTime.put(streamName, Utils.getCurrentMilliseconds());

                if (timerDeltaTime.get(streamName) >= flushInterval_) {
                    if (eventsBuffer.get(streamName).size() > 0) {
                        flushEvent(streamName, streamData_.get(streamName), eventsBuffer.get(streamName));
                        eventsSize.put(streamName, 0);
                    }
                    timerDeltaTime.put(streamName, 0L);
                }

                Event eventObject = eventManager_.getEvent(streamName);
                if (eventObject == null) {
                    continue;
                }

                if (!eventsSize.containsKey(streamName)) {
                    eventsSize.put(streamName, 0);
                }

                if (!eventsBuffer.containsKey(streamName)) {
                    eventsBuffer.put(streamName, new LinkedList<String>());
                }

                int newSize = eventsSize.get(streamName) + eventObject.data_.getBytes().length;
                eventsSize.put(streamName, newSize);
                eventsBuffer.get(streamName).add(eventObject.data_);

                if (eventsSize.get(streamName) >= bulkBytesSize_) {
                    flushEvent(streamName, streamData_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                }

                if (eventsBuffer.get(streamName).size() >= bulkSize_) {
                    flushEvent(streamName, streamData_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                }

                if (isFlushData_) {
                    flushEvent(streamName, streamData_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                }

                if (isClearSize) {
                    eventsSize.put(streamName, 0);
                    timerDeltaTime.put(streamName, 0L);
                }
            }

            if (isFlushData_) {
                isFlushData_ = false;
            }
        }
    }

    /**
     * Flushes the data to atom API
     *
     * @param stream  stream name for saving data in db table
     * @param authKey HMAC auth key for stream
     * @param data    bulk of events to send
     */
    private void flushData(String stream, String authKey, List<String> data) {
        int attempt = 1;

        while (true) {
            Response response = api_.putEvents(stream, data, authKey);
            printLog("stream: " + stream + "; response: " + response.status);
            if (response.status < 500 && response.status > 1) {
                break;
            }

            int duration = (int) (getRetryTime(attempt++) * 1000);
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ex) {
            }

            printLog("Url: " + api_.getEndpoint() + " Retry request: " + data);
        }
    }

    /**
     * Print the log.
     *
     * @param logData data to print
     */
    protected void printLog(String logData) {
        if (isDebug_) {
            synchronized (System.out) {
                System.out.println(TAG_ + ": " + logData);
            }
        }
    }
}
