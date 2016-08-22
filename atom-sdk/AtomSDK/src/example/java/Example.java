import com.ironsrc.atom.*;
import org.json.JSONObject;

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
                        JSONObject eventJson = new JSONObject().put("strings", eventData);
                        if (index < 5) {
                            tracker_.track("ibtest", eventJson.toString(), "");
                        } else {
                            tracker_.track("ibtest2", eventJson.toString(), "");
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

        JSONObject dataGet = new JSONObject().put("strings", "data GET");
        Response responseGet = api_.putEvent(streamGet, dataGet.toString(), authKey, HttpMethod.GET);

        System.out.println("Data: " + responseGet.data + "; Status: " + responseGet.status +
                           "; Error: " + responseGet.error);

        String streamPost = "ibtest";
        String authKeyPost = "";

        JSONObject dataPost = new JSONObject().put("strings", "data POST");
        Response responsePost = api_.putEvent(streamPost, dataPost.toString(), authKeyPost, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data + "; Status: " + responsePost.status +
                           "; Error: " + responsePost.error);

        String streamBulk = "ibtest";
        LinkedList<String> dataBulkList1 = new LinkedList<String>();

        JSONObject dataBulk1 = new JSONObject().put("strings", "test BULK 1");
        dataBulkList1.add(dataBulk1.toString());

        JSONObject dataBulk2 = new JSONObject().put("strings", "test BULK 2");
        dataBulkList1.add(dataBulk2.toString());

        JSONObject dataBulk3 = new JSONObject().put("strings", "test BULK 3");
        dataBulkList1.add(dataBulk3.toString());

        api_.setAuth("");

        Response responseBulk = api_.putEvents(streamBulk, dataBulkList1);

        System.out.println("Data: " + responseBulk.data + "; Status: " + responseBulk.status +
                           "; Error: " + responseBulk.error);

        LinkedList<String> dataBulkList2 = new LinkedList<String>();

        dataBulk1 = new JSONObject().put("strings", "test BULK 1 1");
        dataBulkList2.add(dataBulk1.toString());

        dataBulk2 = new JSONObject().put("strings", "test BULK 1 2");
        dataBulkList2.add(dataBulk2.toString());

        dataBulk3 = new JSONObject().put("strings", "test BULK 1 3");
        dataBulkList2.add(dataBulk3.toString());

        Response responseBulk2 = api_.putEvents(streamBulk, Utils.listToJson(dataBulkList2));

        System.out.println("Data: " + responseBulk2.data + "; Status: " + responseBulk2.status +
                           "; Error: " + responseBulk2.error);
    }
}