package com.ironsrc.atom;

import java.util.HashMap;
import java.util.List;

/**
 * ironSource Atom low level API. supports putEvent() and putEvents() methods.
 */
public class IronSourceAtom {
    private static String TAG_ = "IronSourceAtom";
    protected static String API_VERSION_ = "V1.1.1";
    protected String endpoint_ = "http://track.atom-data.io/";
    protected String authKey_ = "";

    protected HashMap<String, String> headers_ = new HashMap<String, String>();

    protected Boolean isDebug_ = false;

    /**
     * Constructor for low-level API
     */
    public IronSourceAtom() {
        initHeaders();
    }

    /**
     * Enable print debug information
     *
     * @param isDebug debug state
     */
    public void enableDebug(Boolean isDebug) {
        isDebug_ = isDebug;
    }

    /**
     * Init headers
     */
    protected void initHeaders() {
        headers_.put("x-ironsource-atom-sdk-type", "java");
        headers_.put("x-ironsource-atom-sdk-version", IronSourceAtom.API_VERSION_);
    }

    /**
     * Set auth key
     *
     * @param authKey HMAC auth key for stream
     */
    public void setAuth(String authKey) {
        authKey_ = authKey;
    }

    /**
     * Get auth key
     *
     * @return HMAC auth key for stream
     */
    public String getAuth() {
        return authKey_;
    }

    /**
     * Set server host
     *
     * @param endpoint server url
     */
    public void setEndpoint(String endpoint) {
        endpoint_ = endpoint;
    }

    /**
     * Get server host
     *
     * @return server url
     */
    public String getEndpoint() {
        return endpoint_;
    }

    /**
     * Send send a single event to Atom API
     *
     * @param stream  atom stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     * @param method  POST or GET method for HTTP request
     * @return response response from server
     */
    public Response putEvent(String stream, String data, String authKey, HttpMethod method) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        String jsonEvent = createRequestData(stream, data, authKey);
        return this.sendEvent(endpoint_, method, headers_, jsonEvent);
    }

    /**
     * Send send a single event to Atom API
     *
     * @param stream stream name for saving data in db table
     * @param data   user data to send
     * @param method POST or GET method for HTTP request
     * @return response response from server
     */
    public Response putEvent(String stream, String data, HttpMethod method) {
        return this.putEvent(stream, data, "", method);
    }

    /**
     * Send send a single event to Atom API
     *
     * @param stream  stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     * @return response response from server
     */
    public Response putEvent(String stream, String data, String authKey) {
        return this.putEvent(stream, data, authKey, HttpMethod.POST);
    }

    /**
     * Send multiple events (batch) to Atom API
     *
     * @param stream  stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     * @return response response from server
     */
    public Response putEvents(String stream, String data, String authKey) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        HttpMethod method = HttpMethod.POST;
        printLog("Key: " + authKey_);

        String jsonEvent = createRequestData(stream, data, authKey);

        return this.sendEvent(endpoint_ + "bulk", method, headers_, jsonEvent);
    }

    /**
     * Send multiple events (batch) to Atom API
     *
     * @param stream  stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     * @return response response from server
     */
    public Response putEvents(String stream, List<String> data, String authKey) {
        String json = Utils.listToJson(data);
        return this.putEvents(stream, json, authKey);
    }

    /**
     * Send multiple events (batch) to Atom API
     *
     * @param stream stream name for saving data in db table
     * @param data   user data to send
     * @return response from server
     */
    public Response putEvents(String stream, List<String> data) {
        return this.putEvents(stream, data, "");
    }

    /**
     * Send multiple events (batch) to Atom API.
     *
     * @param stream stream name for saving data in db table
     * @param data   user data to send
     * @return response from server
     */
    public Response putEvents(String stream, String data) {
        return this.putEvents(stream, data, "");
    }

    /**
     * Preform health check to Atom API
     */
    public void health() {
        this.sendEvent(endpoint_ + "health", HttpMethod.GET, headers_, "");
    }

    /**
     * Create request json data
     *
     * @param stream  stream name for saving data in db table
     * @param data    user data to send
     * @param authKey HMAC auth key for stream
     * @return request json data
     */
    protected String createRequestData(String stream, String data, String authKey) {
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
     * Send data to Atom API
     *
     * @param url     atom API url
     * @param method  HTTP METHOD (POST or GET)
     * @param headers headers for request
     * @param data    user data to send
     * @return response from server
     */
    protected Response sendEvent(String url, HttpMethod method, HashMap<String, String> headers, String data) {
        Request request = new Request(url, data, headers);
        request.enableDebug(isDebug_);
        return (method == HttpMethod.GET) ? request.get() : request.post();
    }

    /**
     * Prints the log.
     *
     * @param logData print debug data
     */
    protected void printLog(String logData) {
        if (isDebug_) {
            synchronized (System.out) {
                System.out.println(TAG_ + ": " + logData);
            }
        }
    }
}
