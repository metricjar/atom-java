/**
 * Created by g8y3e on 7/22/16.
 */

import com.ironsource.atom.Event;
import org.junit.Assert;
import org.junit.Test;

public class EventTest {
    @Test
    public void testCreateObject() {
        String expectedStream = "test stream";
        String expectedData = "test data";
        String expectedAuth = "test auth";

        Event eventObject = new Event(expectedStream, expectedData, expectedAuth);

        Assert.assertEquals(eventObject.stream_, expectedStream);
        Assert.assertEquals(eventObject.data_, expectedData);
        Assert.assertEquals(eventObject.authKey_, expectedAuth);
    }
}
