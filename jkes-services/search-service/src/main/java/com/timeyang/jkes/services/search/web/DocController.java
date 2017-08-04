package com.timeyang.jkes.services.search.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author chaokunyang
 */
@Controller
@RequestMapping("/docs")
public class DocController {

    @GetMapping
    public String getDoc() {
        return "docs/doc_v1";
    }

}
