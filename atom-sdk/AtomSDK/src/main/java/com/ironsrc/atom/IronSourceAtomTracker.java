/**
 * Created by g8y3e on 7/20/16.
 */
package com.ironsrc.atom;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Iron source atom tracker.
 */
public class IronSourceAtomTracker {
    private static String TAG_ = "IronSourceAtomTracker";

    private int taskWorkersCount_ = 24;
    private int taskPoolSize_ = 10000;

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

    private Boolean isDebug_;
    private Boolean isFlushData_;


    private Boolean isRunWorker_ = true;
    private Thread eventWorkerThread_;

    private ConcurrentHashMap<String, String> streamData_;

    private IEventManager eventManager_;
    private EventTaskPool eventPool_;
    private Random random_;

    /**
     * API Tracker constructor
     */
    public IronSourceAtomTracker() {
        api_ = new IronSourceAtom();
        eventPool_ = new EventTaskPool(taskWorkersCount_, taskPoolSize_);

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
     * Clear craeted IronSourceCoroutineHandler
     */
    public void Stop() {
        isRunWorker_ = false;
        eventPool_.stop();
    }

    /**
     * Sets the size of the task pool.
     * @param taskPoolSize task pool size
     */
    public void SetTaskPoolSize(int taskPoolSize) {
        taskPoolSize_ = taskPoolSize;
    }

    /**
     * Sets the task workers count.
     * @param taskWorkersCount task workers count
     */
    public void SetTaskWorkersCount(int taskWorkersCount) {
        taskWorkersCount_ = taskWorkersCount;
    }

    /**
     * Sets the event manager.
     * @param eventManager custom event manager
     */
    public void SetEventManager(IEventManager eventManager) {
        eventManager_ = eventManager;
    }

    /**
     * Enabling print debug information
     * @param isDebug If set to true is debug.
     */
    public void EnableDebug(Boolean isDebug) {
        isDebug_ = isDebug;

        api_.enableDebug(isDebug);
    }

    /**
     * Set Auth Key for stream
     * @param authKey for secret key of stream.
     */
    public void SetAuth(String authKey) {
        api_.setAuth(authKey);
    }

    /**
     * Set endpoint for send data
     * @param endpoint for address of server
     */
    public void SetEndpoint(String endpoint) {
        api_.setEndpoint(endpoint);
    }

    /**
     * Set Bulk data count
     * @param bulkSize count of event for flush
     */
    public void SetBulkSize(int bulkSize) {
        bulkSize_ = bulkSize;
    }

    /**
     * Set Bult data bytes size
     * @param bulkBytesSize size in bytes
     */
    public void SetBulkBytesSize(int bulkBytesSize) {
        bulkBytesSize_ = bulkBytesSize;
    }

    /**
     * Set intervals for flushing data
     * @param flushInterval intervals in seconds
     */
    public void SetFlushInterval(long flushInterval) {
        flushInterval_ = flushInterval;
    }

    /**
     * Track data to server
     * @param stream name of the stream
     * @param data info for sending
     * @param authKey secret token for stream
     */
    public void Track(String stream, String data, String authKey) {
        if (authKey.length() == 0) {
            authKey = api_.getAuth();
        }

        if (!streamData_.containsKey(stream)) {
            streamData_.putIfAbsent(stream, authKey);
        }

        eventManager_.addEvent(new Event(stream, data, authKey));
    }

    /**
     * Flush all data to server
     */
    public void Flush() {
        isFlushData_ = true;
    }

    /**
     * Gets the duration.
     * @param attempt attempt count
     * @return duration
     */
    private double GetDuration(int attempt) {
        double duration = minTime_ * Math.pow(2, attempt);
        duration = (random_.nextDouble() * (duration - minTime_)) + minTime_;

        if (duration > maxTime_) {
            duration = maxTime_;
        }

        return duration;
    }

    private void flushEvent(String stream, String authKey, LinkedList<String> events) {
        LinkedList<String> buffer = new LinkedList<String>(events);
        events.clear();
        //eventsSize.put(stream, 0);
       // timerDeltaTime = 0;

        eventPool_.addEvent(new EventTask(stream, authKey, buffer) {
            public void action() {
                FlushData(this.stream_, this.authKey_, this.buffer_);
            }
        });
    }

    /**
     * Events the worker.
     */
    private void eventWorker() {
        long timerStartTime = Utils.getCurrentMilliseconds();
        long timerDeltaTime = 0;

        // temporary buffers for hold event data per stream
        HashMap<String, LinkedList<String>> eventsBuffer = new HashMap<String, LinkedList<String>>();
        // buffers size storage
        HashMap<String, Integer> eventsSize = new HashMap<String, Integer>();

        Boolean isClearSize = false;

        while (isRunWorker_) {
            for (Map.Entry<String, String> entry : streamData_.entrySet()) {
                timerDeltaTime += Utils.getCurrentMilliseconds() - timerStartTime;
                timerStartTime = Utils.getCurrentMilliseconds();

                String streamName = entry.getKey();

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

                if (timerDeltaTime >= flushInterval_) {
                    flushEvent(streamName, streamData_.get(streamName), eventsBuffer.get(streamName));
                    isClearSize = true;
                }

                if (isClearSize) {
                    eventsSize.put(streamName, 0);
                    timerDeltaTime = 0;
                }
            }

            if (isFlushData_) {
                isFlushData_ = false;
            }
        }
    }

    /**
     * Flush the data.
     * @param stream name of the stream
     * @param authKey secret key for stream
     * @param data for sending to server
     */
    private void FlushData(String stream, String authKey, LinkedList<String> data) {
        int attempt = 1;

        while (true) {
            Response response = api_.putEvents(stream, data, authKey);
            printLog("data: " + data + "; response: " + response.status);
            if (response.status < 500 && response.status > 1) {
                break;
            }

            int duration = (int)(GetDuration(attempt++) * 1000);
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ex) {
            }

            printLog("Url: " + api_.getEndpoint() + " Retry request: " + data);
        }
    }

    /**
     * Print the log.
     * @param logData data to print
     */
    protected void printLog(String logData) {
        if (isDebug_) {
            System.out.println(TAG_ + ": " + logData);
        }
    }
}
