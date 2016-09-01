import com.google.gson.Gson;
import com.ironsrc.atom.*;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.LinkedList;

public class Example {
    private static Boolean isRunThreads = true;
    private static int threadIndex;

    private static IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();
    private static String stream = "YOUR.STREAM.NAME";
    private static String authKey = "YOUR_HMAC_AUTH_KEY";

    public static void main(String[] args) throws JSONException {
        // Example of using high level API (Tracker)
        System.out.println("Starting ironSource Atom example");
        System.out.println("=== Tracker Example ===");
        // Tracker conf
        tracker_.enableDebug(true); // Enable of debug msg printing
        tracker_.setAuth(authKey); // Set default auth key
        tracker_.setBulkBytesSize(2048); // Set bulk size in bytes (default 512KB)
        //tracker_.setBulkKiloBytesSize(1); // Set bulk size in Kilobytes (default 512KB)
        tracker_.setBulkSize(50); // Set Number of events per bulk (batch) (default: 20)
        tracker_.setFlushInterval(5000); // Set flush interval in ms (default: 30 seconds)
        tracker_.setEndpoint("http://track.atom-data.io/");

        for (int i = 0; i < 10; ++i) {
            threadIndex = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int index = threadIndex;
                    while (isRunThreads) {
                        JSONObject jsonObject = generateRandomData("TRACKER TEST");
                        HashMap<String, String> hashMapObject = new HashMap<String, String>();
                        double randNum = 1000 * Math.random();
                        hashMapObject.put("event_name", "JAVA_SDK_TEST");
                        hashMapObject.put("id", "" + (int) randNum);
                        hashMapObject.put("float_value", "" + randNum);
                        hashMapObject.put("strings", "HASHMAP TRACKER TEST");
                        hashMapObject.put("ts", "" + Utils.getCurrentMilliseconds());
                        if (index < 5) {
                            // Sending a JSONObject
                            tracker_.track(stream, jsonObject.toString(), "");
                            // Sending a Hash Map (using Gson to stringify it)
                            tracker_.track(stream, new Gson().toJson(hashMapObject), ""); // Sending
                        } else {
                            // Send with custom auth key
                            tracker_.track("this.stream.doesnt.exist", jsonObject.toString(), "HMAC AUTH_KEY");
                        }
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            thread.start();
        }

        try {
            Thread.sleep(10500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
        System.out.println("Example: Killing tracker threads");
        tracker_.flush(); // Flush immediately
        tracker_.stop();
        isRunThreads = false;

        // Example of using low level API
        System.out.println("\n\n=== Low level API example ===");

        IronSourceAtom api_ = new IronSourceAtom();

        api_.enableDebug(true); // Enable debug printing
        api_.setEndpoint("http://track.atom-data.io/");
        api_.setAuth(authKey); // Set default auth key

        JSONObject dataLowLevelApi = generateRandomData("JAVA SDK GET METHOD");

        // putEvent Get method test;
        Response responseGet = api_.putEvent(stream, dataLowLevelApi.toString(), authKey, HttpMethod.GET);
        dataLowLevelApi.put("strings", "JAVA SDK POST METHOD");

        // putEvent Post method test
        System.out.println("Data: " + responseGet.data + "; Status: " + responseGet.status +
                "; Error: " + responseGet.error);
        Response responsePost = api_.putEvent(stream, dataLowLevelApi.toString(), authKey, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data + "; Status: " + responsePost.status + "; Error: " +
                responsePost.error);

        // putEvents method test:
        LinkedList<String> batchData = new LinkedList<String>();
        for (int i = 0; i < 10; i++) {
            batchData.add(generateRandomData("JAVA SDK BULK").toString());
        }
        Response responseBulk = api_.putEvents(stream, batchData);
        System.out.println("Data: " + responseBulk.data + "; Status: " + responseBulk.status + "; Error: " + responseBulk.error);
        System.exit(0);
    }

    // Generate JSON with random data
    private static JSONObject generateRandomData(String methodType) {
        double randNum = 1000 * Math.random();
        JSONObject dataLowLevelApi = new JSONObject();
        try {
            dataLowLevelApi.put("event_name", "JAVA_SDK_TEST");
            dataLowLevelApi.put("id", (int) randNum);
            dataLowLevelApi.put("float_value", randNum);
            dataLowLevelApi.put("strings", methodType);
            dataLowLevelApi.put("ts", Utils.getCurrentMilliseconds());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataLowLevelApi;
    }

}