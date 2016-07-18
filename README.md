# ironSource.atom SDK for Java

[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]

atom-java is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for Java.

- [Signup](https://atom.ironsrc.com/#/signup)
- [Documentation](https://ironsource.github.io/atom-java/)
- [Sending an event](#Using-the-IronSource-API-to-send-events)

#### Using the IronSource API to send events 
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