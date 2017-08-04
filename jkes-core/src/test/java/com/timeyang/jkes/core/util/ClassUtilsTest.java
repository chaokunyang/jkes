package com.timeyang.jkes.core.util;

import com.timeyang.jkes.core.annotation.Document;
import org.junit.Test;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class ClassUtilsTest {

    @Test
    public void getCLasses() {
        ClassUtils.getClasses("com.timeyang.search.entity").forEach(System.out::println);

        ClassUtils.getClasses("com.fasterxml.jackson.databind.module").forEach(System.out::println);
    }

    @Test
    public void getAnnotatedClasses() {
        ClassUtils.getAnnotatedClasses("com.timeyang.search.entity", Document.class).forEach(System.out::println);
    }
}