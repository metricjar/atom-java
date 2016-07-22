# ironSource.atom SDK for Java

[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Build status][travis-image]][travis-url]
[![Coverage Status][coverage-image]][coverage-url]

atom-java is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for Java.

- [Signup](https://atom.ironsrc.com/#/signup)
- [Documentation](https://ironsource.github.io/atom-java/)
- [Sending an event](#Using-the-IronSource-API-to-send-events)

#### Using the IronSource API to send events 
##### Tracker usage
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

String dataTrack = "{\"strings\": \"data track\"}";
// add data to queue
tracker_.track("<YOUR_STREAM_NAME>", dataTrack, "<YOUR_AUTH_KEY>");

// send data with default key that was initiated with method setAuth 
tracker_.track("<YOUR_STREAM_NAME>", dataTrack);

// hard flush all data in queue
tracker_.flush();

// stops all workers in task pool
tracker_.stop();
```

##### Interface for store data `IEventManager`.
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

##### Low level API usage
Example of sending an event in Java:
```java
IronSourceAtom api_ = new IronSourceAtom();
api_.enableDebug(true);

String streamGet = "<YOUR_STREAM_NAME>";
String authKey = "<YOUR_AUTH_KEY>";
String dataGet = "{\"strings\": \"data GET\"}";

Response responseGet = api_.putEvent(streamGet, dataGet, authKey, HttpMethod.GET);

System.out.println("Data: " + responseGet.data);

String streamPost = "<YOUR_STREAM_NAME>";
String authKeyPost = "<YOUR_AUTH_KEY>";
String dataPost = "{\"strings\": \"data POST\"}";

Response responsePost = api_.putEvent(streamPost, dataPost, authKeyPost, HttpMethod.POST);

System.out.println("Data: " + responsePost.data);

String streamBulk = "<YOUR_STREAM_NAME>";
LinkedList<String> dataBulk = new LinkedList<String>();
dataBulk.add("{\"strings\": \"test BULK 1\"}");
dataBulk.add("{\"strings\": \"test BULK 2\"}");
dataBulk.add("{\"strings\": \"test BULK 3\"}");

api_.setAuth("<YOUR_AUTH_KEY>");
Response responseBulk = api_.putEvents(streamBulk, dataBulk);

System.out.println("Data: " + responseBulk.data);
```
### License
MIT

[license-image]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square
[license-url]: LICENSE
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/atom-java/
[travis-image]: https://travis-ci.org/ironSource/atom-java.svg?branch=master
[travis-url]: https://travis-ci.org/ironSource/atom-java
[coverage-image]: https://coveralls.io/repos/github/ironSource/atom-java/badge.svg?branch=master
[coverage-url]: https://coveralls.io/github/ironSource/atom-java?branch=master
