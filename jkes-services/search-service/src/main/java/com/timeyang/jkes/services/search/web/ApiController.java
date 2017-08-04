package com.timeyang.jkes.services.search.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author chaokunyang
 */
@Controller
@RequestMapping("/api")
public class ApiController {

    @RequestMapping(value = "/v1", method = RequestMethod.GET)
    public String v1() {
        return "api/v1";
    }

    @RequestMapping(value = "/v2", method = RequestMethod.GET)
    public String v2() {
        return "api/v2";
    }

    @RequestMapping(value = "/v3", method = RequestMethod.GET)
    public String v3() {
        return "api/v3";
    }

}
