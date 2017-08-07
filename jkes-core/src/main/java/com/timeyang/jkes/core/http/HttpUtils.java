package com.timeyang.jkes.core.http;

import com.timeyang.jkes.core.util.Asserts;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author chaokunyang
 */
public class HttpUtils {

    public static String parseIpFromUrl(String url) throws MalformedURLException, UnknownHostException {
        Asserts.notBlank(url, "url can't be null");

        InetAddress address = InetAddress.getByName(new URL(url).getHost());
        return address.getHostAddress();
    }

    public static String[] getIpsFromUrls(String... urls) throws MalformedURLException, UnknownHostException {
        if(urls == null || urls.length == 0)
            return null;

        String[] ips = new String[urls.length];
        for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
            String url = urls[i];

            InetAddress address = InetAddress.getByName(new URL(url).getHost());
            ips[i] = address.getHostAddress();
        }

        return ips;
    }

    public static String getIpsFromDomainName(String domainName) throws UnknownHostException {
        String url = "http://" + domainName;
        try {
            InetAddress address = InetAddress.getByName(new URL(url).getHost());
            return address.getHostAddress();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // impossible, because we add "http://" prefix
        }
    }

    public static String[] getIpsFromDomainNames(String... domainNames) throws UnknownHostException {
        if(domainNames == null || domainNames.length == 0)
            return null;

        String[] ips = new String[domainNames.length];
        for (int i = 0, urlsLength = domainNames.length; i < urlsLength; i++) {
            String url = "http://" + domainNames[i];

            try {
                InetAddress address = InetAddress.getByName(new URL(url).getHost());
                ips[i] = address.getHostAddress();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e); // impossible, because we add "http://" prefix
            }
        }

        return ips;
    }

}
