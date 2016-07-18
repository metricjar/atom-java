package com.ironsrc.atom;

/**
 * Created by g8y3e on 7/18/16.
 */
public class Response {
    public String error;
    public String data;
    public int status;

    public Response(String error, String data, int status) {
        this.error = error;
        this.data = data;
        this.status = status;
    }
}
