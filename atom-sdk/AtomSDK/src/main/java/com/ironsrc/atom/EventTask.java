/**
 * Created by valentine.pavchuk on 7/20/16.
 */
package com.ironsrc.atom;


import java.util.LinkedList;

/**
 * Thread pool task for sending data
 */
public abstract class EventTask {
    protected String stream_;
    protected String authKey_;
    protected LinkedList<String> buffer_;

    /**
     * Constructor for thread pool task
     * @param stream
     * @param authKey
     * @param buffer
     */
    public EventTask(String stream, String authKey, LinkedList<String> buffer) {
        stream_ = stream;
        authKey_ = authKey;
        buffer_ = buffer;
    }

    /**
     * Action for run async
     */
    public abstract void action();
}
