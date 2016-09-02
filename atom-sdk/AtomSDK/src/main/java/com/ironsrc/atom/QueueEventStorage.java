package com.ironsrc.atom;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue event storage (in memory queue that implements Interface Event Storage).
 */
public class QueueEventStorage implements IEventStorage {
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>> streamToEventQueue_;

    /**
     * Initializes a new instance of the QueueEventStorage
     */
    public QueueEventStorage() {
        // HashMap of stream -> Queue of Event's
        streamToEventQueue_ = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Event>>();
    }

    /**
     * Add event to Queue
     *
     * @param eventObject event that will be added to queue
     */
    public void addEvent(Event eventObject) {
        if (!streamToEventQueue_.containsKey(eventObject.stream_)) {
            streamToEventQueue_.putIfAbsent(eventObject.stream_, new ConcurrentLinkedQueue<Event>());
        }

        streamToEventQueue_.get(eventObject.stream_).add(eventObject);
    }

    /**
     * Get events for a given stream
     *
     * @param stream stream to get the events for
     * @return eventObject first event from the event queue
     */
    public Event getEvent(String stream) {
        Event eventObject = null;
        if (streamToEventQueue_.containsKey(stream)) {
            eventObject = streamToEventQueue_.get(stream).poll();
        }

        return eventObject;
    }
}
