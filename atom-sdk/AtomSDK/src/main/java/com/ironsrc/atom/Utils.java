/**
 * Created by g8y3e on 7/18/16.
 */
package com.ironsrc.atom;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

public class Utils {
    /**
     * Convert Object to json string
     * @param object data for convert
     * @return json string
     */
    public static String objectToJson(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * Convert List to json string
     * @param listData data for convert
     * @return json string
     */
    public static String listToJson(LinkedList<String> listData) {
        String resultJson = "[";
        for (String entry: listData) {
            resultJson += entry + ",";
        }

        resultJson = resultJson.substring(0, resultJson.length()-1);
        resultJson += "]";

        return resultJson;
    }

    /**
     * Encode data to HMAC SHA-256
     * @param data data for encode
     * @param key key for encode
     * @return encoded data
     */
    public static String encodeHmac(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Encode data to base64
     * @param data data to encode
     * @param encoding for example "UTF-8"
     * @return encoded data
     */
    public static String base64Encode(String data, String encoding) {
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return encoder.encode(data.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Url encode data for url
     * @param data data to escape
     * @param encoding for example "UTF-8"
     * @return escaped data
     */
    public static String urlEncode(String data, String encoding) {
        try {
            return URLEncoder.encode(data, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}