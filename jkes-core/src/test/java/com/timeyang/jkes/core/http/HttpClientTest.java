package com.timeyang.jkes.core.http;

import org.junit.Test;

import java.net.InetAddress;
import java.net.URL;

/**
 * ${DESCRIPTION}
 *
 * @author chaokunyang
 */
public class HttpClientTest {
    @Test
    public void get() throws Exception {
        Response response = HttpClient.getInstance().get("http://k1-test.com:8084", "");
        System.out.println(response);
    }

    @Test
    public void post() throws Exception {

    }

    @Test
    public void buildUri() throws Exception {
        String url = "http://timeyang.com/popular";
        // String url = "http://es1-dev.com";
        InetAddress address = InetAddress.getByName(new URL(url).getHost());
        String ip = address.getHostAddress();
        System.out.println(ip);
    }

}