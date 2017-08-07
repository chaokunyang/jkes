package com.timeyang.jkes.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author chaokunyang
 */
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JsonOrgModule());
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String convertToString(JSONObject jsonObject) {
        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] convertToBytes(JSONObject jsonObject) {
        try {
            return mapper.writeValueAsBytes(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject readValue(InputStream inputStream) throws IOException {
        return mapper.readValue(inputStream, JSONObject.class);
    }

    public static <T> T parseJsonToObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
