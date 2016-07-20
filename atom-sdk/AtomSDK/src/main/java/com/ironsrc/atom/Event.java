/**
 * Created by g8y3e on 7/20/16.
 */
package com.ironsrc.atom;

public class Event {
    public String data_;
    public String stream_;
    public String authKey_;

    /**
     * Batch constructor
     * @param stream name of stream.
     * @param data data for server.
     * @param authKey secret key for stream
     */
    public Event(String stream, String data, String authKey) {
        stream_ = stream;
        data_ = data;
        authKey_ = authKey;
    }
}
