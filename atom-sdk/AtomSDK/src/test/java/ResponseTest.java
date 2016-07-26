/**
 * Created by g8y3e on 7/22/16.
 */
import com.ironsource.atom.Response;
import org.junit.Assert;
import org.junit.Test;

public class ResponseTest {
    @Test
    public void ResponseProperties_Test() {
        String expectedError = "test error";
        String expectedData = "test data";
        int expectedStatus = 200;

        Response response = new Response(expectedError, expectedData, expectedStatus);

        Assert.assertEquals(expectedError, response.error);
        Assert.assertEquals(expectedData, response.data);
        Assert.assertEquals(expectedStatus, response.status);
    }
}
