package com.ironsrc.atom;

import java.util.List;

/**
 * Holds a batch(bulk) of events and related data for a given stream
 */
public abstract class BatchEvent {
    protected String stream_;
    protected String authKey_;
    protected List<String> buffer_;

    /**
     * Constructor for BatchEvent
     *
     * @param stream  Atom Stream name
     * @param authKey Stream HMAC auth key
     * @param buffer  Buffer with all events for current stream
     */
    public BatchEvent(String stream, String authKey, List<String> buffer) {
        stream_ = stream;
        authKey_ = authKey;
        buffer_ = buffer;
    }

    /**
     * Action for running async
     */
    public abstract void action();
}
