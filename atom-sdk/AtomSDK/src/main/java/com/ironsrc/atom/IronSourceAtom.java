/**
 * Created by g8y3e on 7/18/16.
 */
package com.ironsrc.atom;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * ironSource Atom low level API class, supports putEvent() and putEvents();
 */
public class IronSourceAtom {
    private static String TAG_ = "IronSourceAtom";

    protected static String API_VERSION_ = "V1.0.0";

    protected String endpoint_ = "http://track.atom-data.io/";
    protected String authKey_ = "";

    protected HashMap<String, String> headers_ = new HashMap<String, String>();

    protected Boolean isDebug_ = false;

    /**
     * Constructor for simple API
     */
    public IronSourceAtom() {
        initHeaders();
    }

    /**
     * Enable print debug information
     * @param isDebug debug state
     */
    public void enableDebug(Boolean isDebug) {
        isDebug_ = isDebug;
    }

    /**
     * Init header for server
     */
    protected void initHeaders() {
        headers_.put("x-ironsource-atom-sdk-type", "dotnet");
        headers_.put("x-ironsource-atom-sdk-version", IronSourceAtom.API_VERSION_);
    }

    /**
     * Set auth key
     * @param authKey auth key for stream
     */
    public void setAuth(String authKey) {
        authKey_ = authKey;
    }

    /**
     * Get auth key
     * @return auth key
     */
    public String getAuth() {
        return authKey_;
    }

    /**
     * Set server host
     * @param endpoint server url
     */
    public void setEndpoint(String endpoint) {
        endpoint_ = endpoint;
    }

    /**
     * Get server host
     * @return server url
     */
    public String getEndpoint() {
        return endpoint_;
    }

    /**
     * Send single data to Atom server.
     * @param stream stream name for saving data in db table
     * @param data user data to send
     * @param authKey auth key for stream
     * @param method for POST or GET method for do request
     * @return response from server
     */
    public Response putEvent(String stream, String data, String authKey, HttpMethod method) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        String jsonEvent = GetRequestData(stream, data, authKey);
        return this.sendEvent(endpoint_, method, headers_, jsonEvent);
    }

    /**
     * Send single data to Atom server.
     * @param stream stream name for saving data in db table
     * @param data user data to send
     * @param method for POST or GET method for do request
     * @return response from server
     */
    public Response putEvent(String stream, String data, HttpMethod method) {
        return this.putEvent(stream, data, "", method);
    }

    /**
     * Send single data to Atom server.
     * @param stream stream name for saving data in db table
     * @param data user data to send
     * @param authKey auth key for stream
     * @return response from server
     */
    public Response putEvent(String stream, String data, String authKey) {
        return this.putEvent(stream, data, authKey, HttpMethod.POST);
    }

    /**
     * Send multiple events data to Atom server.
     * @param stream for name of stream
     * @param data for request data
     * @param authKey auth key for stream
     * @return response from server
     */
    public Response putEvents(String stream, String data, String authKey) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        HttpMethod method = HttpMethod.POST;
        printLog("Key: " + authKey_);

        String jsonEvent = GetRequestData(stream, data, authKey);

        return this.sendEvent(endpoint_ + "bulk", method, headers_, jsonEvent);
    }

    /**
     * Send multiple events data to Atom server.
     * @param stream for name of stream
     * @param data for request data
     * @param authKey auth key for stream
     * @return response from server
     */
    public Response putEvents(String stream, LinkedList<String> data,
                                String authKey) {
        String json = Utils.listToJson(data);
        return this.putEvents(stream, json, authKey);
    }

    /**
     * Send multiple events data to Atom server.
     * @param stream for name of stream
     * @param data for request data
     * @return response from server
     */
    public Response putEvents(String stream, LinkedList<String> data) {
       return this.putEvents(stream, data, "");
    }

    /**
     * Send multiple events data to Atom server.
     * @param stream for name of stream
     * @param data for request data
     * @return response from server
     */
    public Response putEvents(String stream, String data) {
        return this.putEvents(stream, data, "");
    }

    /**
     * Check health of server
     */
    public void health() {
        this.sendEvent(endpoint_ + "health", HttpMethod.GET, headers_, "");
    }

    /**
     * Create request json data
     * @param stream for request stream
     * @param data for request data
     * @param authKey auth key for stream
     * @return request json data
     */
    protected String GetRequestData(String stream, String data, String authKey) {
        String hash = "";
        if (authKey.length() > 0) {
            hash = Utils.encodeHmac(data, authKey);
        } 


        HashMap<String, String> eventObject = new HashMap<String, String>();
        eventObject.put("table", stream);
        eventObject.put("data", data);
        if (authKey.length() > 0) {
            eventObject.put("auth", hash);
        }
        String jsonEvent = Utils.objectToJson(eventObject);

        printLog("Request body: " + jsonEvent);

        return jsonEvent;
    }

    /**
     * Send data to server
     * @param url for server address
     * @param method for POST or GET method
     * @param headers headers data for request
     * @param data for request data
     * @return response from server
     */
    protected Response sendEvent(String url, HttpMethod method, HashMap<String, String> headers,
                                 String data) {
        Request request = new Request(url, data, headers);
        request.enableDebug(isDebug_);
        return (method == HttpMethod.GET) ? request.Get() : request.Post();
    }

    /**
     * Prints the log.
     * @param logData  print debug data
     */
    protected void printLog(String logData) {
        if (isDebug_) {
            System.out.println(TAG_ + ": " + logData);
        }
    }
}
