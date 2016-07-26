import com.ironsource.atom.*;

import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
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

        // set event pool size and worker threads count
        tracker_.setTaskPoolSize(1000);
        tracker_.setTaskWorkersCount(24);

        // test for bulk size
       // tracker_.setBulkBytesSize(2);
        //tracker_.setBulkSize(4);
        tracker_.setFlushInterval(2000);
        tracker_.setEndpoint("http://track.atom-data.io/");

        for (int i = 0; i < 10; ++i) {
            threadIndex = i;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int index = threadIndex;
                    while (isRunThreads) {
                        String data = "{\"strings\": \"d: " + requestIndex.incrementAndGet() +
                                " t: " + Thread.currentThread().getId() + "\"}";

                        if (index < 5) {
                            tracker_.track("ibtest", data, "");
                        } else {
                            tracker_.track("ibtest2", data, "");
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
        String dataGet = "{\"strings\": \"data GET\"}";

        Response responseGet = api_.putEvent(streamGet, dataGet, authKey, HttpMethod.GET);

        System.out.println("Data: " + responseGet.data + "; Status: " + responseGet.status +
                           "; Error: " + responseGet.error);

        String streamPost = "ibtest";
        String authKeyPost = "";
        String dataPost = "{\"strings\": \"data POST\"}";

        Response responsePost = api_.putEvent(streamPost, dataPost, authKeyPost, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data + "; Status: " + responsePost.status +
                           "; Error: " + responsePost.error);

        String streamBulk = "ibtest";
        LinkedList<String> dataBulk = new LinkedList<String>();
        dataBulk.add("{\"strings\": \"test BULK 1\"}");
        dataBulk.add("{\"strings\": \"test BULK 2\"}");
        dataBulk.add("{\"strings\": \"test BULK 3\"}");

        api_.setAuth("");

        Response responseBulk = api_.putEvents(streamBulk, dataBulk);

        System.out.println("Data: " + responseBulk.data + "; Status: " + responseBulk.status +
                           "; Error: " + responseBulk.error);

        LinkedList<String> dataBulk2 = new LinkedList<String>();
        dataBulk2.add("{\"strings\": \"test BULK 1 1\"}");
        dataBulk2.add("{\"strings\": \"test BULK 1 2\"}");
        dataBulk2.add("{\"strings\": \"test BULK 1 3\"}");

        Response responseBulk2 = api_.putEvents(streamBulk, Utils.listToJson(dataBulk2));

        System.out.println("Data: " + responseBulk2.data + "; Status: " + responseBulk2.status +
                           "; Error: " + responseBulk2.error);
    }
}