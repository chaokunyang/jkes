package com.timeyang.jkes.core.util;

import org.json.JSONObject;
import org.junit.Test;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class JsonUtilsTest {
    @Test
    public void convertToString() throws Exception {
        JSONObject jsonObject = new JSONObject();
        // "settings" : {
        //     "index" : {
        //         "number_of_shards" : 3,
        //                 "number_of_replicas" : 2
        //     }
        // }
        jsonObject.put("settings",
                new JSONObject().put("index",
                        new JSONObject().put("number_of_shards", 3)
                                        .put("number_of_replicas", 2)
                )
        );

        String settings = JsonUtils.convertToString(jsonObject);
        System.out.println(settings);
    }

}