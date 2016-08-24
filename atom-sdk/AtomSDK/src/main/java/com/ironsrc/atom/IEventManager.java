package com.ironsrc.atom;

/**
 * Interface for storing data in a backlog
 */
public interface IEventManager {

    /**
     * Add an event.
     *
     * @param eventObject event data object
     */
    void addEvent(Event eventObject);

    /**
     * Get one event from data store
     *
     * @param stream name of the atom stream
     * @return event object
     */
    Event getEvent(String stream);
}
