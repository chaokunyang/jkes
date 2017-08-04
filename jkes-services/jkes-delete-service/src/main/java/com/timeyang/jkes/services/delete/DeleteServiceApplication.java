package com.timeyang.jkes.services.delete;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author chaokunyang
 */
public class DeleteServiceApplication {

    public static void main(String[] args) {
        Properties properties = getProperties("delete_service.properties");


    }

    public static Properties getProperties(String name) {
        Properties properties = new Properties();

        InputStream resourceAsStream = DeleteServiceApplication.class.getClassLoader().getResourceAsStream(name);


        try {
            try (final InputStream stream =
                         DeleteServiceApplication.class.getClassLoader().getResourceAsStream(name)) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
