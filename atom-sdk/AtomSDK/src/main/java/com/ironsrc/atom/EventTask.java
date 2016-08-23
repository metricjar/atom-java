/**
 * Created by g8y3e on 7/20/16.
 */
package com.ironsrc.atom;

import java.util.List;

/**
 * Thread pool task for sending data
 */
public abstract class EventTask {
    protected String stream_;
    protected String authKey_;
    protected List<String> buffer_;

    /**
     * Constructor for thread pool task
     * @param stream
     * @param authKey
     * @param buffer
     */
    public EventTask(String stream, String authKey, List<String> buffer) {
        stream_ = stream;
        authKey_ = authKey;
        buffer_ = buffer;
    }

    /**
     * Action for run async
     */
    public abstract void action();
}
