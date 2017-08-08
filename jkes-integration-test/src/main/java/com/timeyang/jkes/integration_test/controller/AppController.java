package com.timeyang.jkes.integration_test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author chaokunyang
 */
@Controller
public class AppController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("api/search")
    public String search() {
        return "search/search";
    }

}
