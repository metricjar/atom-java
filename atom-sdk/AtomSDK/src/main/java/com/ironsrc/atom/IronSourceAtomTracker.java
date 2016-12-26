package com.ironsrc.atom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ironSource Atom high level API class (Tracker), supports: track() and flush()
 */
public class IronSourceAtomTracker {
    private static String TAG_ = "IronSourceAtomTracker";

    // Number of concurrent worker threads for BatchEventPool workers
    private static int BATCH_WORKERS_COUNT_ = 5;

    // Number of batch events inside BatchEventPool
    private static int BATCH_POOL_SIZE_ = 5000;

    // Tracker flush interval in milliseconds
    private long flushInterval_ = 30000;

    //Number of events per bulk (batch)
    private int bulkLength_ = 200;

    //The size of the bulk in bytes.
    private int bulkBytesSize_ = 512 * 1024;

    // Jitter time conf
    private double minTime_ = 1;
    private double maxTime_ = 10;

    private IronSourceAtom api_;

    private Boolean isDebug_ = false;
    public volatile Boolean isFlushData_ = false;

    private Boolean isRunWorker_ = true;
    private Thread eventWorkerThread_;

    // Holds the auth-key for each stream which doesn't use the default key
    private ConcurrentHashMap<String, String> streamToAuthMap_;

    // Backlog of single events (placed here after .track is being called)
    private IEventStorage eventsBacklog_;
    // Backlog of batch events that are ready to be handled (sent to Atom) by workers (threads)
    private BatchEventPool batchEventPool_;
    private Random random_;
    private Timer flushTimer;

    /**
     * Atom Tracker constructor
     */
    public IronSourceAtomTracker() {
        this(BATCH_WORKERS_COUNT_, BATCH_POOL_SIZE_);
    }

    /**
     * Atom Tracker constructor
     *
     * @param workerCount   amount of workers (threads) for concurrent event handling
     * @param batchPoolSize amount of BatchEvent's ({stream,auth,buffer}) to store in BatchEventPool
     */
    public IronSourceAtomTracker(int workerCount, int batchPoolSize) {
        api_ = new IronSourceAtom();
        batchEventPool_ = new BatchEventPool(workerCount, batchPoolSize);
        eventsBacklog_ = new QueueEventStorage();
        streamToAuthMap_ = new ConcurrentHashMap<String, String>();

        random_ = new Random();
        flushTimer = new Timer();

        eventWorkerThread_ = new Thread(new Runnable() {
            public void run() {
                trackerHandler();
            }
        });

        // Flush records every {flushInterval} seconds
        setTimer();
        eventWorkerThread_.start();
    }

    private void setTimer() {
        flushTimer.cancel();
        // Set Flush Interval
        flushTimer = new Timer();
        flushTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                isFlushData_ = true;
            }
        }, 0, flushInterval_);
    }

    /**
     * Stop all event pool threads
     */
    public void stop() {
        printLog("Tracker stop method");
        isRunWorker_ = false;
        batchEventPool_.stop();
        flushTimer.cancel();
        flushTimer.purge();
    }

    /**
     * Sets the storage manager
     * This function is here for backwards compatibility reasons
     *
     * @param eventStorage custom backlog events storage
     */
    public void setEventManager(IEventStorage eventStorage) {
        eventsBacklog_ = eventStorage;
    }

    /**
     * Sets the storage manager
     *
     * @param eventStorage custom backlog events storage
     */
    public void setEventStorage(IEventStorage eventStorage) {
        eventsBacklog_ = eventStorage;
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
     * This function is here for backwards compatibility reasons
     *
     * @param bulkSize upon reaching this amount of events in buffer, flush the buffer
     */
    public void setBulkSize(int bulkSize) {
        bulkLength_ = bulkSize;
    }

    /**
     * Set bulk size (amount of events) for flush
     *
     * @param bulkLength upon reaching this amount of events in buff, flush the buffer
     */
    public void setBulkLength(int bulkLength) {
        bulkLength_ = bulkLength;
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
     * Set bulk size in KiloBytes
     *
     * @param bulkKiloBytesSize upon reaching this size, flush the buffer
     */
    public void setBulkKiloBytesSize(int bulkKiloBytesSize) {
        bulkBytesSize_ = bulkKiloBytesSize * 1024;
    }

    /**
     * Set data flushing interval
     *
     * @param flushInterval flush the events every {flush interval} ms
     */
    public void setFlushInterval(long flushInterval) {
        flushInterval_ = flushInterval;
        setTimer();
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

        if (!streamToAuthMap_.containsKey(stream)) {
            streamToAuthMap_.putIfAbsent(stream, authKey);
        }
        eventsBacklog_.addEvent(new Event(stream, data, authKey));
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
        // Clone the events list in order to clear the trackerHandler buffer
        List<String> buffer = new LinkedList<String>(events);
        events.clear();

        try {
            batchEventPool_.addEvent(new BatchEvent(stream, authKey, buffer) {
                public void action() {
                    if(this.buffer_.size() > 0) {
                        flushData(this.stream_, this.authKey_, this.buffer_);
                    }
                }
            });
        } catch (Exception ex) {
            printLog(ex.getMessage());
        }
    }

    /**
     * Main tracker handler function, handles the flushing conditions.
     * Flushes on the following conditions:
     * Every 30 seconds (default)
     * Number of accumulated events has reached 500 (default)
     * Size of accumulated events has reached 512KB (default)
     */
    private void trackerHandler() {

        // Temporary buffers for holding event data (payload) per stream
        HashMap<String, LinkedList<String>> eventsBuffer = new HashMap<String, LinkedList<String>>();
        // Buffers size storage
        HashMap<String, Integer> eventsSize = new HashMap<String, Integer>();
        // Clear size buffer
        Boolean isClearSize = false;
        // Clear flushData variable
        boolean isClearFlush = false;

        while (isRunWorker_) {
            for (Map.Entry<String, String> entry : streamToAuthMap_.entrySet()) {
                String streamName = entry.getKey();
                Event eventObject = eventsBacklog_.getEvent(streamName);
                if (eventObject == null) {
                    continue;
                }

                if (!eventsSize.containsKey(streamName)) {
                    eventsSize.put(streamName, 0);
                }

                if (!eventsBuffer.containsKey(streamName)) {
                    eventsBuffer.put(streamName, new LinkedList<String>());
                }

                // Calculate new size in bytes for all events and store it in eventsSize map
                int newSize = eventsSize.get(streamName) + eventObject.data_.getBytes().length;
                eventsSize.put(streamName, newSize);

                // Store the currently handled event into a temp buffer
                eventsBuffer.get(streamName).add(eventObject.data_);

                // Flush when reaching {bulkByteSize} KB of events
                if (isFlushData_) {  // Force flush
                    printLog("Flushing, Force flush called");
                    flushEvent(streamName, streamToAuthMap_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                    // We don't set isFlushData_ to "false" here since we can have multiple streams.
                    // It will be set to "false" after the `foreach loop` has been finished.
                    isClearFlush = true;
                } else if (eventsSize.get(streamName) >= bulkBytesSize_) {
                    printLog("Flushing, bulk size reached: " + eventsSize.get(streamName));
                    flushEvent(streamName, streamToAuthMap_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                } else if (eventsBuffer.get(streamName).size() >= bulkLength_) { // Flush when {bulkLength} (amount of events) has been reached
                    printLog("Flushing, bulk length reached: " + eventsBuffer.get(streamName).size());
                    flushEvent(streamName, streamToAuthMap_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                }

                if (isClearSize) {
                    eventsSize.put(streamName, 0);
                    isClearSize = false;
                }
            }
            if (isClearFlush) {
                isFlushData_ = false;
                isClearFlush = false;
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
