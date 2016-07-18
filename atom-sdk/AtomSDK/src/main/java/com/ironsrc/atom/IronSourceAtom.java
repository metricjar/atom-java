package com.ironsrc.atom;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by g8y3e on 7/18/16.
 */
public class IronSourceAtom {
    private static String TAG_ = "IronSourceAtom";

    protected static String API_VERSION_ = "V1.0.0";

    protected String endpoint_ = "http://track.atom-data.io/";
    protected String authKey_ = "";

    protected HashMap<String, String> headers_ = new HashMap<String, String>();

    protected Boolean isDebug_ = false;

    public IronSourceAtom() {
        initHeaders();
    }

    public void enableDebug(Boolean isDebug) {
        isDebug_ = isDebug;
    }

    protected void initHeaders() {
        headers_.put("x-ironsource-atom-sdk-type", "dotnet");
        headers_.put("x-ironsource-atom-sdk-version", IronSourceAtom.API_VERSION_);
    }

    public void setAuth(String authKey) {
        authKey_ = authKey;
    }

    public String getAuth() {
        return authKey_;
    }

    public void setEndpoint(String endpoint) {
        endpoint_ = endpoint;
    }

    public String getEndpoint() {
        return endpoint_;
    }

    public Response putEvent(String stream, String data, String authKey, HttpMethod method) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        String jsonEvent = GetRequestData(stream, data, authKey);
        return this.sendEvent(endpoint_, method, headers_, jsonEvent);
    }

    public Response putEvent(String stream, String data, HttpMethod method) {
        return this.putEvent(stream, data, "", method);
    }

    public Response putEvent(String stream, String data, String authKey) {
        return this.putEvent(stream, data, authKey, HttpMethod.POST);
    }

    public Response putEvents(String stream, String data, String authKey) {
        if (authKey.length() == 0) {
            authKey = authKey_;
        }

        HttpMethod method = HttpMethod.POST;
        printLog("Key: " + authKey_);

        String jsonEvent = GetRequestData(stream, data, authKey);

        return this.sendEvent(endpoint_ + "bulk", method, headers_, jsonEvent);
    }

    public Response putEvents(String stream, LinkedList<String> data,
                                String authKey) {
        String json = Utils.listToJson(data);
        return this.putEvents(stream, json, authKey);
    }

    public Response putEvents(String stream, LinkedList<String> data) {
       return this.putEvents(stream, data, "");
    }

    public void health() {
        this.sendEvent(endpoint_ + "health", HttpMethod.GET, headers_, "");
    }

    protected String GetRequestData(String stream, String data, String authKey) {
        String hash = Utils.encodeHmac(data, authKey);

        HashMap<String, String> eventObject = new HashMap<String, String>();
        eventObject.put("table", stream);
        eventObject.put("data", data);
        eventObject.put("auth", hash);
        String jsonEvent = Utils.objectToJson(eventObject);

        printLog("Request body: " + jsonEvent);

        return jsonEvent;
    }

    protected Response sendEvent(String url, HttpMethod method, HashMap<String, String> headers,
                                 String data) {
        Request request = new Request(url, data, headers);
        request.enableDebug(isDebug_);
        return (method == HttpMethod.GET) ? request.Get() : request.Post();
    }

    protected void printLog(String logData) {
        if (isDebug_) {
            System.out.println(TAG_ + ": " + logData);
        }
    }
}
