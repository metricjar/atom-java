package com.ironsrc.test;

import com.google.gson.Gson;
import com.ironsrc.atom.IronSourceAtom;
import com.ironsrc.atom.IronSourceAtomTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MultiThreadTest {
    private static String stream = "YOUR.STREAM.NAME";
    private static String authKey = "YOUR_HMAC_AUTH_KEY";

    static IronSourceAtom atom_;
    static IronSourceAtomTracker atomTracker_ = new IronSourceAtomTracker();

    static {
        atomTracker_.enableDebug(true);
        atomTracker_.setAuth(authKey);
        atomTracker_.setBulkBytesSize(2048);
        atomTracker_.setBulkSize(2);
        atomTracker_.setFlushInterval(2000);
        atomTracker_.setEndpoint("http://track.atom-data.io/");
    }

    public static void runTest() {
        List<Thread> threads = new ArrayList<>();
        for (int index = 0; index < 10; ++index) {
            final int threadID = index;
            Thread threadObj = new Thread(new Runnable() {
                @Override
                public void run() {
                    int randTimer = (int) (2000 * Math.random());
                    try {
                        Thread.sleep(randTimer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("From thread id: " + threadID);

                    for (int reqIndex = 0; reqIndex < 6; reqIndex++) {
                        HashMap<String, String> dataMap = new HashMap<String, String>();

                        dataMap.put("strings", "data " + reqIndex);
                        dataMap.put("id", "" + threadID);

                        System.out.println("Request: " + new Gson().toJson(dataMap));
                        atomTracker_.track(stream, new Gson().toJson(dataMap));
                    }
                }
            });
            threadObj.run();

            threads.add(threadObj);
        }

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        atomTracker_.flush();
    }
}

public class ExampleStaticThreads {
    public static void main(String ... argc) {
        MultiThreadTest.runTest();
    }
}