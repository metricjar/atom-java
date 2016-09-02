package com.ironsrc.atom;

public class Response {
    public String error;
    public String data;
    public int status;

    /**
     * Constructor for Response
     *
     * @param error  Atom API response error message
     * @param data   Atom API response data
     * @param status Atom API response status
     */
    public Response(String error, String data, int status) {
        this.error = error;
        this.data = data;
        this.status = status;
    }
}
