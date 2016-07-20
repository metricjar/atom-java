/**
 * Created by valentine.pavchuk on 7/20/16.
 */
package com.ironsrc.atom;


import java.util.LinkedList;

public abstract class EventTask {
    protected String stream_;
    protected String authKey_;
    protected LinkedList<String> buffer_;

    public EventTask(String stream, String authKey, LinkedList<String> buffer) {
        stream_ = stream;
        authKey_ = authKey;
        buffer_ = buffer;
    }

    public abstract void action();
}
