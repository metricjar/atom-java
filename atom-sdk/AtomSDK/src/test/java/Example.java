import com.ironsrc.atom.HttpMethod;
import com.ironsrc.atom.IronSourceAtom;
import com.ironsrc.atom.Response;
import com.ironsrc.atom.Utils;

import java.util.LinkedList;

public class Example {
    public static void main(String [] args) {
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