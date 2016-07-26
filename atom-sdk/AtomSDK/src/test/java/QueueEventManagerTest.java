/**
 * Created by g8y3e on 7/22/16.
 */
import com.ironsource.atom.Event;
import com.ironsource.atom.IEventManager;
import com.ironsource.atom.QueueEventManager;
import org.junit.Assert;
import org.junit.Test;

public class QueueEventManagerTest {
    @Test
    public void testCreateObject() {
        IEventManager eventManager = new QueueEventManager();

        Assert.assertEquals(eventManager.getEvent(""), null);
    }

    @Test
    public void testEventGetAdd() {
        IEventManager eventManager = new QueueEventManager();
        String streamName = "test stream";

        Event expectedEvent = new Event(streamName, "test data", "test auth");
        eventManager.addEvent(expectedEvent);

        Event resultEvent = eventManager.getEvent(streamName);

        Assert.assertEquals(expectedEvent.stream_, resultEvent.stream_);
        Assert.assertEquals(expectedEvent.data_, resultEvent.data_);
        Assert.assertEquals(expectedEvent.authKey_, resultEvent.authKey_);
    }
}
