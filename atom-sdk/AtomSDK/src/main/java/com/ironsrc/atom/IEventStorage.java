package com.ironsrc.atom;

/**
 * Interface for providing a generic way of storing events in a backlog before they are sent to Atom.
 */
public interface IEventStorage {

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
