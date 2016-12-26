# ironSource.atom SDK for Java

[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Build status][travis-image]][travis-url]
[![Coverage Status][coverage-image]][coverage-url]
[![Maven Status][maven-image]][maven-url]

atom-java is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for Java.

- [Signup](https://atom.ironsrc.com/#/signup)
- [Documentation](https://ironsource.github.io/atom-java/)
- [Installation](#installation)
- [Usage](#usage)
- [Change Log](#change-log)
- [Example](#example)

## Installation

### Installation for Gradle Project
Add dependency for Atom SDK
```ruby
dependencies {
   compile 'com.ironsrc.atom:atom-sdk:1.5.1'
}
```

### Installation for Maven Project
Add dependency for Atom SDK
```xml
<dependencies>
    <dependency>
        <groupId>com.ironsrc.atom</groupId>
        <artifactId>atom-sdk</artifactId>
        <version>1.5.1</version>
    </dependency>
</dependencies>
```

## Usage

### High Level API - "Tracker"
The tracker is used for sending events to Atom based on several conditions
- Every 30 seconds (default)
- Number of accumulated events has reached 200 (default)
- Size of accumulated events has reached 512KB (default)

To see the full code check the [example section](#example)
```java
public class Example {
    private static Boolean isRunThreads = true;
    private static int threadIndex;

    private static IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();
    private static String stream = "YOUR.STREAM.NAME";
    private static String authKey = "HMAC_AUTH_KEY";

    public static void main(String[] args) throws JSONException {
        // Tracker conf
        tracker_.enableDebug(true); // Enable of debug msg printing
        tracker_.setAuth(authKey); // Set default auth key
        tracker_.setBulkBytesSize(2048); // Set bulk size in bytes (default 512KB)
        //tracker_.setBulkKiloBytesSize(1); // Set bulk size in Kilobytes (default 512KB)
        tracker_.setBulkSize(50); // Set Number of events per bulk (batch) (default: 200)
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
                            tracker_.track("ibtest2", jsonObject.toString(), "HMAC AUTH_KEY");
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
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
        System.out.println("Example: Killing tracker threads");
        tracker_.stop();
        isRunThreads = false;
        System.exit(0);
    }
}
```
### Low Level API usage
The Low Level API has 2 methods:  
- putEvent - Sends a single event to Atom  
- putEvents - Sends a bulk (batch) of events to Atom
  
To see the full code check the [example section](#example)
```java
public class Example {
    public static void main(String[] args) throws JSONException {
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
}
```

### Interface for storing data at the tracker backlog `IEventStorage`
**Implementation must to be synchronized for multi threading use.**
```java
/**
 * Interface for providing a generic way of storing events in a backlog before they are sent to Atom.
 */
public interface IEventStorage {

    /**
     * Add an event.
     *
     * @param eventObject event data object
     */
    void addEvent(Event eventObject);

    /**
     * Get one event from data store
     *
     * @param stream name of the atom stream
     * @return event object
     */
    Event getEvent(String stream);
}
```

Using custom event storage implementation:
```java
IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();
// Class CustomStorageManager must implement interface IEventStorage and must be synchronized
IEventStorage customStorageManager = new CustomStroageManger();
tracker_.setEventStorage(customStorageManager);
```

## Change Log

### v1.5.1
- Fixed a bug in tracker that caused several conditions to flush at the same time
- Added example with static usage of the SDK

### v1.5.0
Note: this version if fully compatible with the old ones except for class name changes.  
The usage of the tracker and Atom class methods should not be affected.
- Added a Maven Repository in Maven Central
- Tracker changes
    - Changed the flushInterval control
    - Fixing a bug in isClearSize boolean var (was not set to false after iteration)
    - eventWorker renamed to trackerHandler
    - Changed some variables and internal function names
    - fixed a bug in tracker pool size and thread count and changed default thread count
- Atom Class changes
    - Renamed GetRequestData function to createRequestData
- Renamed EventManager interface to EventStorage
- Renamed QueueEventManager to QueueEventStorage
- Renamed EventTask class to BatchEvent
- Renamed EventTaskPool class to BatchEventPool
- Rewrote and improved all the JavaDocs
- Refactored and improved the example
    
### v1.1.0
- Tracker Class
- EventManager (EventStorage) interface
- Maven repository (in GitHub)

### v1.0.0
- Basic features: putEvent & putEvents functionalities


## Example
Full example of all SDK features can be found [here](atom-sdk/AtomSDK/src/example/java/)

## License
[MIT][license-url]

[license-image]: https://img.shields.io/badge/license-MIT-blue.svg
[license-url]: LICENSE
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/atom-java/
[travis-image]: https://travis-ci.org/ironSource/atom-java.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-java
[coverage-image]: https://coveralls.io/repos/github/ironSource/atom-java/badge.svg?branch=master
[coverage-url]: https://coveralls.io/github/ironSource/atom-java?branch=master
[maven-image]: https://img.shields.io/badge/maven%20build-v1.5.0-green.svg
[maven-url]: http://search.maven.org/#artifactdetails%7Ccom.ironsrc.atom%7Catom-sdk%7C1.5.0%7Cjar
