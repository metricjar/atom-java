import com.google.gson.Gson;
import com.ironsrc.atom.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Example {
    static Boolean isRunThreads = true;
    static AtomicInteger requestIndex = new AtomicInteger(0);
    static int threadIndex;

    static IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();

    public static void main(String [] args) {
        /**
         * Example of using high level API
         */
        tracker_.enableDebug(true);
        tracker_.setAuth("");

        // test for bulk size
        tracker_.setBulkBytesSize(2);
        tracker_.setBulkSize(4);
        tracker_.setFlushInterval(2000);
        tracker_.setEndpoint("http://track.atom-data.io/");

        for (int i = 0; i < 10; ++i) {
            threadIndex = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int index = threadIndex;
                    while (isRunThreads) {
                        String eventData = "d: " + requestIndex.incrementAndGet() +
                                " t: " + Thread.currentThread().getId();

                        HashMap<String, String> eventJson = new HashMap<String, String>();
                        eventJson.put("strings",eventData);
                        if (index < 5) {
                            tracker_.track("ibtest", new Gson().toJson(eventJson), "");
                        } else {
                            tracker_.track("ibtest2", new Gson().toJson(eventJson), "");
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                        }

                        if (requestIndex.get() >= 34) {
                            isRunThreads = false;
                        }
                    }
                }
            });

            thread.start();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
        }

        tracker_.stop();

        /**
         * Example of using low level API
         */
        IronSourceAtom api_ = new IronSourceAtom();

        api_.enableDebug(true);
        api_.setEndpoint("http://track.atom-data.io/");

        String streamGet = "ibtest";
        String authKey = "";

        HashMap<String, String> dataGet = new HashMap<String, String>();
        dataGet.put("strings", "data GET");
        Response responseGet = api_.putEvent(streamGet, new Gson().toJson(dataGet), authKey, HttpMethod.GET);

        System.out.println("Data: " + responseGet.data + "; Status: " + responseGet.status +
                           "; Error: " + responseGet.error);

        String streamPost = "ibtest";
        String authKeyPost = "";

        HashMap<String, String> dataPost = new HashMap<String, String>();
        dataPost.put("strings", "data POST");
        Response responsePost = api_.putEvent(streamPost, new Gson().toJson(dataPost), authKeyPost, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data + "; Status: " + responsePost.status +
                           "; Error: " + responsePost.error);

        String streamBulk = "ibtest";
        LinkedList<String> dataBulkList1 = new LinkedList<String>();

        HashMap<String, String> dataBulk1 = new HashMap<String, String>();
        dataBulk1.put("strings", "data BULK 1");
        dataBulkList1.add(new Gson().toJson(dataBulk1));

        HashMap<String, String> dataBulk2 = new HashMap<String, String>();
        dataBulk2.put("strings", "data BULK 2");
        dataBulkList1.add(new Gson().toJson(dataBulk2));

        HashMap<String, String> dataBulk3 = new HashMap<String, String>();
        dataBulk3.put("strings", "data BULK 3");
        dataBulkList1.add(new Gson().toJson(dataBulk3));

        api_.setAuth("");

        Response responseBulk = api_.putEvents(streamBulk, dataBulkList1);

        System.out.println("Data: " + responseBulk.data + "; Status: " + responseBulk.status +
                           "; Error: " + responseBulk.error);

        LinkedList<HashMap<String, String>> dataBulkList2 = new LinkedList<HashMap<String, String>>();

        HashMap<String, String> dataBulk11 = new HashMap<String, String>();
        dataBulk11.put("strings", "data BULK 1 1");
        dataBulkList2.add(dataBulk11);

        HashMap<String, String> dataBulk12 = new HashMap<String, String>();
        dataBulk12.put("strings", "data BULK 1 2");
        dataBulkList2.add(dataBulk12);

        HashMap<String, String> dataBulk13 = new HashMap<String, String>();
        dataBulk13.put("strings", "data BULK 1 3");
        dataBulkList2.add(dataBulk13);

        Response responseBulk2 = api_.putEvents(streamBulk, Utils.objectToJson(dataBulkList2));

        System.out.println("Data: " + responseBulk2.data + "; Status: " + responseBulk2.status +
                           "; Error: " + responseBulk2.error);
    }
}