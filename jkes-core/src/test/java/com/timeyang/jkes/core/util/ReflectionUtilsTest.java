package com.timeyang.jkes.core.util;

import com.timeyang.jkes.entity.ComplexEntity;
import com.timeyang.jkes.entity.PersonGroup;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class ReflectionUtilsTest {
    @Test
    public void getReturnTypeParameters() throws Exception {
        Method method = PersonGroup.class.getDeclaredMethod("getPersons");
        ReflectionUtils.getReturnTypeParameters(method);
    }

    @Test
    public void getActualReturnType() throws Exception {
        Method method1 = ComplexEntity.class.getDeclaredMethod("getCars");
        System.out.println(ReflectionUtils.getInnermostType(method1));

        Method method2 = ComplexEntity.class.getDeclaredMethod("getBooks");
        System.out.println(ReflectionUtils.getInnermostType(method2));
    }

}