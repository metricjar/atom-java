/**
 * Created by valentine.pavchuk on 7/20/16.
 */
package com.ironsrc.atom;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue event manager.
 */
public class QueueEventManager implements IEventManager {
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>> events_;

    /**
     * Initializes a new instance of the QueueEventManager
     */
    public QueueEventManager() {
        events_ = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>>();
    }

    /**
     * Add event to storage
     * @param eventObject event for adding to storage
     */
    public void addEvent(Event eventObject) {
        if (!events_.containsKey(eventObject.stream_)) {
            events_.putIfAbsent(eventObject.stream_, new ConcurrentLinkedQueue<Event>());
        }

        events_.get(eventObject.stream_).add(eventObject);
    }

    /**
     * Get Event from storage
     * @param stream name of the stream
     * @return
     */
    public Event getEvent(String stream) {
        Event eventObject = null;
        if (events_.containsKey(stream)) {
            eventObject = events_.get(stream).poll();
        }

        return eventObject;
    }
}
