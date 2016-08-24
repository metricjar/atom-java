package com.ironsrc.atom;

/**
 * Holds event data (stream name, data itself and auth key for stream)
 * Used inside the Tracker
 */

public class Event {
    public String data_;
    public String stream_;
    public String authKey_;

    /**
     * Batch constructor
     *
     * @param stream  name of stream.
     * @param data    data for server.
     * @param authKey secret key for stream
     */
    public Event(String stream, String data, String authKey) {
        stream_ = stream;
        data_ = data;
        authKey_ = authKey;
    }
}
