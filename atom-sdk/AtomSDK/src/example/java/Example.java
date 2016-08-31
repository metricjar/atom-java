import com.google.gson.Gson;
import com.ironsrc.atom.*;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.LinkedList;

public class Example {
    private static Boolean isRunThreads = true;
    private static int threadIndex;

    private static IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();
    private static String stream = "sdkdev_sdkdev.public.zeev";
    private static String authKey = "I40iwPPOsG3dfWX30labriCg9HqMfL";

    public static void main(String[] args) throws JSONException {
        // Example of using high level API (Tracker)
        tracker_.enableDebug(true);
        tracker_.setAuth(authKey);
        System.out.println("Starting ironSource Atom example");
        System.out.println("=== Tracker Example ===");

        // Test for bulk size
        tracker_.setBulkBytesSize(2048);
        tracker_.setBulkSize(50);
        tracker_.setFlushInterval(5000);
        tracker_.setEndpoint("http://track.atom-data.io/");

        for (int i = 0; i < 1; ++i) {
            threadIndex = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int index = threadIndex;
                    while (isRunThreads) {
                        JSONObject jsonObject = generateRandomData("TRACKER TEST");
                        if (index < 5) {
                            tracker_.track(stream, jsonObject.toString(), "");
                        } else {
                            tracker_.track("ibtest2", new Gson().toJson(jsonObject), "");
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            });

            thread.start();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
        System.out.println("Example: Killing Threads");
        tracker_.stop();
        isRunThreads = false;

        // Example of using low level API
        System.out.println("\n\n=== Low level API example ===");

        IronSourceAtom api_ = new IronSourceAtom();

        api_.enableDebug(true);
        api_.setEndpoint("http://track.atom-data.io/");
        api_.setAuth(authKey);

        JSONObject dataLowLevelApi = generateRandomData("GET METHOD TEST");

        // putEvent Get method test;

        Response responseGet = api_.putEvent(stream, new Gson().toJson(dataLowLevelApi), authKey, HttpMethod.GET);
        dataLowLevelApi.put("strings", "POST METHOD TEST");

        // putEvent Post method test
        System.out.println("Data: " + responseGet.data + "; Status: " + responseGet.status +
                "; Error: " + responseGet.error);
        Response responsePost = api_.putEvent(stream, new Gson().toJson(dataLowLevelApi), authKey, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data + "; Status: " + responsePost.status + "; Error: " +
                responsePost.error);

        // putEvents method test:
        LinkedList<String> batchData = new LinkedList<String>();
        for (int i = 0; i < 10; i++) {
            batchData.add(new Gson().toJson(generateRandomData("BULK TEST")));
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
            dataLowLevelApi.put("event_name", "JAVA_TRACKER");
            dataLowLevelApi.put("id", (int) randNum);
            dataLowLevelApi.put("float_value", randNum);
            dataLowLevelApi.put("strings", methodType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataLowLevelApi;
    }

}