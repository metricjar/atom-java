package com.ironsrc.atom;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

/**
 * Created by g8y3e on 7/18/16.
 */
public class Utils {
    public static String objectToJson(Object object) {
        return new Gson().toJson(object);
    }

    public static String listToJson(LinkedList<String> listData) {
        String resultJson = "[";
        for (String entry: listData) {
            resultJson += entry + ",";
        }

        resultJson = resultJson.substring(0, resultJson.length()-1);
        resultJson += "]";

        return resultJson;
    }

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

    public static String base64Encode(String data) {
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return encoder.encode(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }
}
