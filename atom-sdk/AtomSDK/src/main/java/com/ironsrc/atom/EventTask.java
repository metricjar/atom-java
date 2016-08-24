package com.ironsrc.atom;

import java.util.List;

/**
 * This class holds the streams and related data inside the EventTaskPool
 */
public abstract class EventTask {
    protected String stream_;
    protected String authKey_;
    protected List<String> buffer_;

    /**
     * Constructor for EventTask
     *
     * @param stream  Atom Stream name
     * @param authKey Stream HMAC auth key
     * @param buffer  Buffer with all events for current stream
     */
    public EventTask(String stream, String authKey, List<String> buffer) {
        stream_ = stream;
        authKey_ = authKey;
        buffer_ = buffer;
    }

    /**
     * Action for running async
     */
    public abstract void action();
}
