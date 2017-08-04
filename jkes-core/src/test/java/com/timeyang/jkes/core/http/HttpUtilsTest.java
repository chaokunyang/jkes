package com.timeyang.jkes.core.http;

import org.junit.Test;

import java.util.Arrays;

/**
 *
 * @author chaokunyang
 */
public class HttpUtilsTest {
    @Test
    public void getIpsFormUrls() throws Exception {
        System.out.println(Arrays.toString(HttpUtils.getIpsFormUrls("http://timeyang.com")));
    }

    @Test
    public void getIpsFormDomainNames() throws Exception {
        System.out.println(Arrays.toString(HttpUtils.getIpsFormDomainNames("timeyang.com", "github.com")));
    }
}