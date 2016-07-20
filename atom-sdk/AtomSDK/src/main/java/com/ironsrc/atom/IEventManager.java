/**
 * Created by valentine.pavchuk on 7/20/16.
 */
package com.ironsrc.atom;

/**
 * Interface for store data
 */
public interface IEventManager {

    /**
     * Add the event.
     * @param eventObject event data object
     */
    public void addEvent(Event eventObject);

    /**
     * Get one the event from store.
     * @param stream name of the stream
     * @return event object
     */
    public Event getEvent(String stream);
}
