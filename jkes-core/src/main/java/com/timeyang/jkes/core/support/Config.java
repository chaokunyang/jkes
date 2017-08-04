package com.timeyang.jkes.core.support;

import org.apache.http.util.Asserts;

/**
 * Config
 * @author chaokunyang
 */
public abstract class Config {

    private volatile static JkesProperties jkesProperties;

    public static JkesProperties getJkesProperties() {
        Asserts.notNull(jkesProperties, "JkesProperties config must be set into Config");
        return jkesProperties;
    }

    /**
     * publish jkesProperties reference, so it is visible to other thread.
     * the jkesProperties should be immutable object
     * @param jkesProperties jkesProperties
     */
    public static void setJkesProperties(JkesProperties jkesProperties) {
        if(JkesProperties.check(jkesProperties))
            Config.jkesProperties = jkesProperties;
    }
}
