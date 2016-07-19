import com.ironsrc.atom.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by g8y3e on 7/18/16.
 */

public class UtilsTest {
    @Test
    public void testObjectToJson() {
        String expectedStr = "{\"test 1\":\"data 1\",\"test 2\":\"data 2\"}";

        TreeMap<String, String> testDict = new TreeMap<String, String>();
        testDict.put("test 1", "data 1");
        testDict.put("test 2", "data 2");

        Assert.assertEquals(expectedStr, Utils.objectToJson(testDict));
    }

    @Test
    public void testListToJson() {
        String expectedStr = "[{\"test\": \"data 1\"},{\"test\": \"data 2\"}]";

        LinkedList<String> testList = new LinkedList<String>();
        testList.add("{\"test\": \"data 1\"}");
        testList.add("{\"test\": \"data 2\"}");

        Assert.assertEquals(expectedStr, Utils.listToJson(testList));
    }

    @Test
    public void testEncodeHmac() {
        String expectedStr = "1861387e46c3001593a644f3ade069a38bbf9a3220e82da5280a1bae1c44e4dc";

        String testInput = "{\"test\": \"data 1\"}";
        String testKey = "FefwefFESRWEfewrvw";

        Assert.assertEquals(expectedStr, Utils.encodeHmac(testInput, testKey));
    }

    @Test
    public void testEncodeHmacException() {
        String expectedStr = "";

        String testInput = "{\"test\": \"data 1\"}";
        String testKey = "";

        Assert.assertEquals(expectedStr, Utils.encodeHmac(testInput, testKey));
    }

    @Test
    public void testBase64Encode() {
        String expectedStr = "eyJ0ZXN0IjogImRhdGEgMSJ9";

        String testData = "{\"test\": \"data 1\"}";

        String resultData = Utils.base64Encode(testData, "UTF-8");

        Assert.assertEquals(expectedStr, resultData);
    }

    @Test
    public void testBase64EncodeException() {
        String expectedStr = "";

        String testData = "{\"test\": \"data 1\"}";

        String resultData = Utils.base64Encode(testData, "UTF-81");

        Assert.assertEquals(expectedStr, resultData);
    }

    @Test
    public void testUrlEncode() {
        String expectedStr = "%7B%22test%22%3A+%22data+1%22%7D";

        String testData = "{\"test\": \"data 1\"}";

        String resultData = Utils.urlEncode(testData, "UTF-8");

        Assert.assertEquals(expectedStr, resultData);
    }

    @Test
    public void testUrlEncodeException() {
        String expectedStr = "";

        String testData = "{\"test\": \"data 1\"}";

        String resultData = Utils.urlEncode(testData, "UTF-81");

        Assert.assertEquals(expectedStr, resultData);
    }
}
