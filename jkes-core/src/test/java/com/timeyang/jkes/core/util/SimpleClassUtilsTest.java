package com.timeyang.jkes.core.util;

import org.junit.Test;

import java.io.IOException;

/**
 * @author chaokunyang
 */
public class SimpleClassUtilsTest {

    @Test
    public void testGetClasses() throws IOException, ClassNotFoundException {
        Class[] classes = SimpleClassUtils.getClasses("com.timeyang.search.entity");
        for (Class c : classes) {
            System.out.println(c);
        }

        for (Class c : SimpleClassUtils.getClasses("com.fasterxml.jackson.databind.module")) {
            System.out.println(c);
        }


    }

}