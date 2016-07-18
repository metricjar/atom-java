import com.ironsrc.atom.HttpMethod;
import com.ironsrc.atom.IronSourceAtom;
import com.ironsrc.atom.Response;

public class Example {
    public static void main(String [] args) {
        IronSourceAtom api_ = new IronSourceAtom();

        api_.enableDebug(true);

        String streamGet = "ibtest";
        String dataGet = "{\"strings\": \"data GET\"}";
        Response responseGet = api_.putEvent(streamGet, dataGet, "", HttpMethod.GET);
    }
}