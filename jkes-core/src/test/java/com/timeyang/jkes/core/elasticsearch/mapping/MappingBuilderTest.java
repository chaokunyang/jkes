package com.timeyang.jkes.core.elasticsearch.mapping;

import com.timeyang.jkes.core.util.JsonUtils;
import com.timeyang.jkes.entity.PersonGroup;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class MappingBuilderTest {

    @Test
    public void testTypeInfer() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Method method = MappingBuilder.class.getDeclaredMethod("inferFieldType", Method.class);
        method.setAccessible(true);

        Method target = PersonGroup.class.getDeclaredMethod("getId");
        method.invoke(MappingBuilder.class, target);
    }

    @Test
    public void buildMapping() {
        JSONObject mapping = new MappingBuilder().buildMapping(PersonGroup.class);
        System.out.println(JsonUtils.convertToString(mapping));
    }
}