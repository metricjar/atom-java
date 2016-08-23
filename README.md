# ironSource.atom SDK for Java

[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Build status][travis-image]][travis-url]
[![Coverage Status][coverage-image]][coverage-url]
[![Maven Status][maven-image]][maven-url]

atom-java is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for Java.

- [Signup](https://atom.ironsrc.com/#/signup)
- [Documentation](https://ironsource.github.io/atom-java/)
- [Sending an event](#Using-the-IronSource-API-to-send-events)

## Instalation for Gradle Project
Add add dependency for Atom SDK
```java
dependencies {
   compile 'com.ironsrc.atom:atom-sdk:1.1.0'
}
```

## Installation for Maven Project
Add dependency for Atom SDK
```xml
<dependencies>
    <dependency>
        <groupId>com.ironsrc.atom</groupId>
        <artifactId>atom-sdk</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

## Using the IronSource API to send events 
### Tracker usage
Example of track an event in Java:
```java
IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();
tracker_.enableDebug(true);
tracker_.setAuth("<YOUR_AUTH_KEY>");

// set event pool size and worker threads count
tracker_.setTaskPoolSize(1000);
tracker_.setTaskWorkersCount(24);

// set bulk size and flush intervall
tracker_.setBulkSize(4);
tracker_.setFlushInterval(2000);
tracker_.setEndpoint("http://track.atom-data.io/");

HashMap<String, String> dataTrack = new HashMap<String, String>();
dataTrack.put("strings", "data track");
// add data to queue
tracker_.track("<YOUR_STREAM_NAME>", new Gson().toJson(dataTrack), "<YOUR_AUTH_KEY>");

// send data with default key that was initiated with method setAuth 
tracker_.track("<YOUR_STREAM_NAME>", dataTrack);

// hard flush all data in queue
tracker_.flush();

// stops all workers in task pool
tracker_.stop();
```

### Interface for store data `IEventManager`.
Implementation must to be synchronized for multithreading use.
```java
/**
 * Interface for store data
 */
public interface IEventManager {

    /**
     * Add the event.
     * @param eventObject event data object
     */
    public void addEvent(Event eventObject);

    /**
     * Get one the event from store.
     * @param stream name of the stream
     * @return event object
     */
    public Event getEvent(String stream);
}
```
Using custom storage implementation:
```java
IronSourceAtomTracker tracker_ = new IronSourceAtomTracker();

IEventManager customEventManager = new QueueEventManager();
tracker_.setEventManager(customEventManager);
```

### Low level API usage
Example of sending an event in Java:
```java
IronSourceAtom api_ = new IronSourceAtom();
api_.enableDebug(true);

String streamGet = "<YOUR_STREAM_NAME>";
String authKey = "<YOUR_AUTH_KEY>";
HashMap<String, String> dataGet = new HashMap<String, String>();
dataGet.put("strings", "data GET");

Response responseGet = api_.putEvent(streamGet, new Gson().toJson(dataGet), authKey, HttpMethod.GET);

System.out.println("Data: " + responseGet.data);

String streamPost = "<YOUR_STREAM_NAME>";
String authKeyPost = "<YOUR_AUTH_KEY>";
HashMap<String, String> dataPost = new HashMap<String, String>();
dataPost.put("strings", "data POST");

Response responsePost = api_.putEvent(streamPost, new Gson().toJson(dataPost), authKeyPost, HttpMethod.POST);

System.out.println("Data: " + responsePost.data);

String streamBulk = "<YOUR_STREAM_NAME>";
LinkedList<String> dataBulk = new LinkedList<String>();

HashMap<String, String> dataBulk1 = new HashMap<String, String>();
dataBulk1.put("strings", "data BULK 1");
dataBulk.add(new Gson().toJson(dataBulk1));

HashMap<String, String> dataBulk2 = new HashMap<String, String>();
dataBulk2.put("strings", "data BULK 2");
dataBulk.add(new Gson().toJson(dataBulk2));

HashMap<String, String> dataBulk3 = new HashMap<String, String>();
dataBulk3.put("strings", "data BULK 3");
dataBulk.add(new Gson().toJson(dataBulk3));

api_.setAuth("<YOUR_AUTH_KEY>");
Response responseBulk = api_.putEvents(streamBulk, dataBulk);

System.out.println("Data: " + responseBulk.data);
```
## License
MIT

[license-image]: https://img.shields.io/badge/license-MIT-blue.svg
[license-url]: LICENSE
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/atom-java/
[travis-image]: https://travis-ci.org/ironSource/atom-java.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-java
[coverage-image]: https://coveralls.io/repos/github/ironSource/atom-java/badge.svg?branch=master
[coverage-url]: https://coveralls.io/github/ironSource/atom-java?branch=master
[maven-image]: https://img.shields.io/badge/maven%20build-v1.1.0-green.svg
[maven-url]: http://search.maven.org/#artifactdetails%7Ccom.ironsrc.atom%7Catom-sdk%7C1.1.0%7Cjar
