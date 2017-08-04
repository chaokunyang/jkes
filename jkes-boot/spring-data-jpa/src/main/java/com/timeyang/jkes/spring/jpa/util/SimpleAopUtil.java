package com.timeyang.jkes.spring.jpa.util;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * @author chaokunyang
 */
public class SimpleAopUtil {

    @SuppressWarnings({"unchecked"})
    public static <T> T getTargetObject(Object proxy) throws Exception {
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return (T) getTargetObject(((Advised)proxy).getTargetSource().getTarget());
        }
        return (T) proxy; // expected to be cglib proxy then, which is simply a specialized class
    }

}
