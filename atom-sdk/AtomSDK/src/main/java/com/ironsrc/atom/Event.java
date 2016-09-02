package com.ironsrc.atom;

/**
 * Holds a single atom event data (stream name, data itself and auth key for stream)
 */

public class Event {
    public String data_;
    public String stream_;
    public String authKey_;

    /**
     * Event class constructor
     *
     * @param stream  Atom Stream name
     * @param authKey Stream HMAC auth key
     * @param data    Stringified event data
     */
    public Event(String stream, String data, String authKey) {
        stream_ = stream;
        data_ = data;
        authKey_ = authKey;
    }
}
