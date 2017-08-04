package com.timeyang.jkes.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author chaokunyang
 */
public class IOUtils {

    /**
     * Read {@link InputStream} as String
     * <p>Note this doesn't close InputStream</p>
     * @param inputStream InputStream
     * @return String representation of InputStream content
     */
    public static String readValue(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try(
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        return stringBuilder.toString();
    }

}
