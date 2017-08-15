package com.timeyang.jkes.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non instantiable class for service registry and access
 *
 * @author chaokunyang
 */
public class Services {

    private Services() {} // prevent instantiation

    private static final Map<String, Provider> providers = new ConcurrentHashMap<>();
    public static final String DEFAULT_PROVIDER_NAME = "default";

    // Provider registration API
    public static void registerDefaultProvider(Provider p) {
        registerProvider(DEFAULT_PROVIDER_NAME, p);
    }

    private static void registerProvider(String name, Provider p) {
        providers.put(name, p);
    }

    // Service access API
    public static Service newInstance() {
        return newInstance(DEFAULT_PROVIDER_NAME);
    }

    private static Service newInstance(String name) {
        Provider p = providers.get(name);
        if(p == null)
            throw new IllegalArgumentException("No provider registered with name: " + name);

        return p.newService();
    }

}
