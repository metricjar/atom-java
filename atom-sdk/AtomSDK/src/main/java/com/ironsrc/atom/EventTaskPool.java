/**
 * Created by g8y3e on 7/20/16.
 */
package com.ironsrc.atom;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventTaskPool {
    private ConcurrentLinkedQueue<EventTask> events_;
    private Boolean isRunning_;

    private LinkedList<Thread> workers_;

    private int maxEvents_;

    /**
     * Initializes a new instance of the ironsource.EventTaskPool class.
     * @param maxThreads max thread for event pool
     * @param maxEvents max events for event pool
     */
    public EventTaskPool(int maxThreads, int maxEvents) {
        maxEvents_ = maxEvents;
        //events_ = new ConcurrentQueue<Action>();
        isRunning_ = true;

        workers_ = new LinkedList<Thread>();

        for (int index = 0; index < maxThreads; ++index) {
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

       /* foreach (Thread thread in workers_) {
            thread.Join();
        }*/
    }

    /**
     * Tasks the worker.
     */
    private void taskWorker() {
        while (isRunning_) {
            EventTask eventTask = events_.poll();
            if (eventTask != null) {
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
     * Add the event.
     * @param eventTask event callback action
     */
    public void addEvent(EventTask eventTask) {
        if (events_.size() > maxEvents_) {
            //throw new EventTaskPoolException("Exceeded max event count in Event Task Pool!");
        }
        events_.add(eventTask);
    }
}
