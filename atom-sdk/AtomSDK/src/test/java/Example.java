import com.ironsrc.atom.HttpMethod;
import com.ironsrc.atom.IronSourceAtom;
import com.ironsrc.atom.Response;

import java.util.LinkedList;

public class Example {
    public static void main(String [] args) {
        IronSourceAtom api_ = new IronSourceAtom();

        api_.enableDebug(true);

        String streamGet = "sdkdev_sdkdev.public.g8y3etest";
        String authKey = "I40iwPPOsG3dfWX30labriCg9HqMfL";
        String dataGet = "{\"strings\": \"data GET\"}";

        Response responseGet = api_.putEvent(streamGet, dataGet, authKey, HttpMethod.GET);

        System.out.println("Data: " + responseGet.data);

        String streamPost = "sdkdev_sdkdev.public.g8y3etest";
        String authKeyPost = "I40iwPPOsG3dfWX30labriCg9HqMfL";
        String dataPost = "{\"strings\": \"data POST\"}";

        Response responsePost = api_.putEvent(streamPost, dataPost, authKeyPost, HttpMethod.POST);

        System.out.println("Data: " + responsePost.data);

        String streamBulk = "sdkdev_sdkdev.public.g8y3etest";
        LinkedList<String> dataBulk = new LinkedList<String>();
        dataBulk.add("{\"strings\": \"test BULK 1\"}");
        dataBulk.add("{\"strings\": \"test BULK 2\"}");
        dataBulk.add("{\"strings\": \"test BULK 3\"}");

        api_.setAuth("I40iwPPOsG3dfWX30labriCg9HqMfL");

        Response responseBulk = api_.putEvents(streamBulk, dataBulk);

        System.out.println("Data: " + responseBulk.data);
    }
}