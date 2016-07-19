/**
 * Created by g8y3e on 7/18/16.
 */
package com.ironsrc.atom;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private static String TAG_ = "Request";

    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000; // 10s

    protected String url_;
    protected String data_;
    protected HashMap<String, String> headers_;

    protected Boolean isDebug_;

    /**
     * Constructor for Request
     * @param url for server address
     * @param data for sending data
     * @param headers for sending headers
     */
    public Request(String url, String data, HashMap<String, String> headers) {
        url_ = url;
        data_ = data;
        headers_ = headers;
    }

    /**
     * Enable print debug information
     * @param isDebug debug state
     */
    public void enableDebug(Boolean isDebug) {
        isDebug_ = isDebug;
    }

    /**
     * GET request to server
     * @return response from server
     */
    public Response Get() {
        String encoding = "UTF-8";
        String url = url_ + "?data=" + Utils.urlEncode(Utils.base64Encode(data_, encoding), encoding);
        printLog("Request URL: " + url);

        return sendRequest(url, "GET");
    }

    /**
     * POST request to server
     * @return response from server
     */
    public Response Post() {
        printLog("Request URL: " + url_);

        return sendRequest(url_, "POST");
    }

    /**
     * Sends the request.
     * @param url server url
     * @param method send data
     * @return response from server
     */
    private Response sendRequest(String url, String method) {
        String data = "";
        String error = "";
        int status = -1;

        HttpURLConnection connection = null;
        BufferedReader inputReader = null;
        DataOutputStream outStream = null;

        try {
            connection = createConnection(url);

            // optional default is GET
            connection.setRequestMethod(method);

            //add request header
            for (Map.Entry<String, String> entry : headers_.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setRequestProperty("Content-Type", "application/json");

            if (method.equals("POST")) {
                connection.setDoOutput(true);
                outStream = new DataOutputStream(connection.getOutputStream());
                outStream.write(data_.getBytes("UTF-8"));
                outStream.flush();
                outStream.close();
            }

            status = connection.getResponseCode();
            inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = inputReader.readLine()) != null) {
                response.append(inputLine);
            }

            data = response.toString();
        } catch (IOException ex) {
            error = ex.getMessage();

            try {
                status = connection.getResponseCode();
            } catch (IOException responseException) {
                printLog("Can't get error status! Error: " + responseException.getMessage());
            }
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
            if (null != inputReader) {
                try {
                    inputReader.close();
                } catch (IOException inputException) {
                    printLog("Can't close input stream! Error: " + inputException.getMessage());
                }
            }
        }

        return new Response(error, data, status);
    }

    /**
     * Create Http Url connection
     * @param url host server url
     * @return http url object
     * @throws IOException
     */
    private HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);
        connection.setDoInput(true);
        return connection;
    }

    /**
     * Prints the log.
     * @param data print debug data
     */
    protected void printLog(String data) {
        System.out.println(TAG_ + ": " + data);
    }
}
