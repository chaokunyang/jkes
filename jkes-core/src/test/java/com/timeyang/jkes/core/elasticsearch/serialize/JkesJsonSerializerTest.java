package com.timeyang.jkes.core.elasticsearch.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.util.ClassUtils;
import com.timeyang.jkes.entity.Person;
import com.timeyang.jkes.entity.PersonGroup;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class JkesJsonSerializerTest {

    @Test
    public void serializeAnnotatedType() throws Exception {
        PersonGroup group = new PersonGroup(1L, "group1", "思考、距离、俘获", new ArrayList<Person>(), "距离产生美");
        Person p1 = new Person(1L, "chaokunyang", "男", 23, "快与慢", group);
        Person p2 = new Person(1L, "chaokunyang", "男", 23, "快与慢", group);
        group.getPersons().add(p1);
        group.getPersons().add(p2);


        SimpleModule module = new SimpleModule();
        Set<Class<?>> annotatedClasses = ClassUtils.getAnnotatedClasses("com.timeyang.search.entity", Document.class);
        annotatedClasses.forEach(aClass -> module.addSerializer(aClass, new JkesJsonSerializer<>()));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String groupResult = mapper.writeValueAsString(group); // 序列化

        assertThat(groupResult, containsString("group1"));
        assertNotEquals(groupResult, containsString("距离产生美"));
        assertNotEquals(groupResult, containsString("快与慢"));
        System.out.println(groupResult);

        String p1Result = mapper.writeValueAsString(p1); // 序列化
        assertNotEquals(p1Result, containsString("chaokunyang"));
        System.out.println(p1Result);

        String p2Result = mapper.writeValueAsString(p2); // 序列化
        assertNotEquals(p2Result, containsString("chaokunyang"));
        System.out.println(p2Result);
    }

    @Test
    public void testMethodReturnType() throws NoSuchMethodException {
        Method getPersons = PersonGroup.class.getDeclaredMethod("getPersons");
        System.out.println("------------------getPersons------------------");
        System.out.println(getPersons.getReturnType()); // class 对象
        System.out.println(getPersons.getGenericReturnType().getClass());
        // 以下方法java8才支持
        System.out.println( getPersons.getGenericReturnType().getTypeName()); // 字符串 java.util.List<com.timeyang.search.entity.Person>

        System.out.println("------------------getId------------------");
        Method getId = PersonGroup.class.getDeclaredMethod("getId");
        System.out.println(getId.getReturnType()); // class 对象
        System.out.println(getId.getGenericReturnType().getClass());

        System.out.println( getId.getGenericReturnType().getTypeName()); // 字符串 java.lang.Long
    }

}