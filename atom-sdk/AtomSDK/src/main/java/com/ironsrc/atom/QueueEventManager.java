package com.ironsrc.atom;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue event manager (in memory Q that implements Interface EventManager).
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
     * Add event to Queue
     *
     * @param eventObject event that will be added to queue
     */
    public void addEvent(Event eventObject) {
        if (!events_.containsKey(eventObject.stream_)) {
            events_.putIfAbsent(eventObject.stream_, new ConcurrentLinkedQueue<Event>());
        }

        events_.get(eventObject.stream_).add(eventObject);
    }

    /**
     * Get events for a given stream
     *
     * @param stream stream to get the events for
     * @return eventObject first event from the event queue
     */
    public Event getEvent(String stream) {
        Event eventObject = null;
        if (events_.containsKey(stream)) {
            eventObject = events_.get(stream).poll();
        }

        return eventObject;
    }
}
