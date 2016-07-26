/**
 * Created by g8y3e on 7/18/16.
 */
package com.ironsrc.atom;

public class Response {
    public String error;
    public String data;
    public int status;

    /**
     * Constructor for Response
     * @param error for server reponse error message
     * @param data for server response data
     * @param status for server reponse status
     */
    public Response(String error, String data, int status) {
        this.error = error;
        this.data = data;
        this.status = status;
    }
}
