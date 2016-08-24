package com.ironsrc.atom;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventTaskPool {
    // List of events inside a Linked Queue for efficient growing and shrinking
    private ConcurrentLinkedQueue<EventTask> events_;
    private Boolean isRunning_;
    // List of workers that send currently to Atom
    private LinkedList<Thread> workers_;
    private int maxEvents_;

    /**
     * Exception for Event Task Pool
     */
    public class EventTaskPoolException extends Exception {
        /**
         * Custom exception constructor
         *
         * @param message error message
         */
        public EventTaskPoolException(String message) {
            super(message);
        }
    }

    /**
     * Initializes a new instance of the EventTaskPool class.
     *
     * @param maxWorkers max threads for event pool
     * @param maxEvents  max events for event pool
     */
    public EventTaskPool(int maxWorkers, int maxEvents) {
        maxEvents_ = maxEvents;
        events_ = new ConcurrentLinkedQueue<EventTask>();
        isRunning_ = true;

        workers_ = new LinkedList<Thread>();

        // Initialize {maxWorkers} amount of threads that will handle sending all events for each stream
        for (int index = 0; index < maxWorkers; ++index) {
            Thread workerThread = new Thread(new Runnable() {
                public void run() {
                    taskWorker();
                }
            });
            workers_.add(workerThread);

            workerThread.start();
        }
    }

    /**
     * Stop this instance.
     */
    public void stop() {
        isRunning_ = false;
    }

    /**
     * Worker task function - handles the event sending for each worker
     */
    private void taskWorker() {
        while (isRunning_) {
            EventTask eventTask = events_.poll();
            if (eventTask == null) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex) {
                }
                continue;
            }
            eventTask.action();
        }
    }

    /**
     * Add worker to task pool
     *
     * @param eventTask event callback action
     */
    public void addEvent(EventTask eventTask) throws EventTaskPoolException {
        if (events_.size() > maxEvents_) {
            throw new EventTaskPoolException("Exceeded max event count in Event Task Pool!");
        }
        events_.add(eventTask);
    }
}
