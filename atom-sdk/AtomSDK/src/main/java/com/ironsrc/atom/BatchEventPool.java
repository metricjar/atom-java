package com.ironsrc.atom;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles concurrent event sending
 * Handles the backlog of BatchEvents
 */
public class BatchEventPool {
    // List of events inside a Linked Queue for efficient growing and shrinking
    private ConcurrentLinkedQueue<BatchEvent> batchEventsQueue_;
    private Boolean isRunning_;
    // List of workers that send currently to Atom
    private LinkedList<Thread> workers_;
    private int maxEvents_;

    /**
     * Exception for Batch Event Pool
     */
    public class BatchEventPoolException extends Exception {
        /**
         * Custom exception constructor
         *
         * @param message error message
         */
        public BatchEventPoolException(String message) {
            super(message);
        }
    }

    /**
     * Initializes a new instance of the BatchEventPool class.
     *
     * @param maxWorkers max threads for event pool
     * @param maxEvents  max events for event pool
     */
    public BatchEventPool(int maxWorkers, int maxEvents) {
        maxEvents_ = maxEvents;
        batchEventsQueue_ = new ConcurrentLinkedQueue<BatchEvent>();
        isRunning_ = true;

        workers_ = new LinkedList<Thread>();

        // Initialize {maxWorkers} amount of threads that handle sending batch events
        for (int index = 0; index < maxWorkers; ++index) {
            Thread workerThread = new Thread(new Runnable() {
                public void run() {
                    batchWorkerTask();
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
     * Batch worker task function - each worker (thread) is polling the Queue for a batch event
     * and handles the sending of the data
     */
    private void batchWorkerTask() {
        while (isRunning_) {
            BatchEvent batchEvent = batchEventsQueue_.poll();
            if (batchEvent == null) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex) {
                }
                continue;
            }
            batchEvent.action();
        }
    }

    /**
     * Add worker to task pool
     *
     * @param batchEvent event callback action
     * @throws BatchEventPoolException thrown if batchEventQueue is bigger than maxEvents
     */
    public void addEvent(BatchEvent batchEvent) throws BatchEventPoolException {
        if (batchEventsQueue_.size() > maxEvents_) {
            throw new BatchEventPoolException("Exceeded max event count in BatchEventPool!");
        }
        batchEventsQueue_.add(batchEvent);
    }
}
