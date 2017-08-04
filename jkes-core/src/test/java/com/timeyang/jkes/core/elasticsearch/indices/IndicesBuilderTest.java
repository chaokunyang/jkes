package com.timeyang.jkes.core.elasticsearch.indices;

import com.timeyang.jkes.core.util.JsonUtils;
import com.timeyang.jkes.entity.PersonGroup;
import org.json.JSONObject;
import org.junit.Test;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class IndicesBuilderTest {
    @Test
    public void buildIndex() throws Exception {
        IndicesBuilder indicesBuilder = IndicesBuilder.getInstance();
        JSONObject indices = indicesBuilder.buildIndex(PersonGroup.class);

        System.out.println(JsonUtils.convertToString(indices));
    }

}