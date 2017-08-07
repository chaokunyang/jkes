package com.timeyang.jkes.core.http;

import org.junit.Test;

import java.util.Arrays;

/**
 *
 * @author chaokunyang
 */
public class HttpUtilsTest {
    @Test
    public void parseIpFromUrl() throws Exception {
        System.out.println(HttpUtils.parseIpFromUrl("http://timeyang.com"));
    }

    @Test
    public void getIpsFormUrls() throws Exception {
        System.out.println(Arrays.toString(HttpUtils.getIpsFromUrls("http://timeyang.com")));
    }

    @Test
    public void getIpsFormDomainNames() throws Exception {
        System.out.println(Arrays.toString(HttpUtils.getIpsFromDomainNames("timeyang.com", "github.com")));
    }
}