package com.timeyang.jkes.integration_test.api;

import com.timeyang.jkes.spring.jpa.index.IndexProgress;
import com.timeyang.jkes.spring.jpa.index.Indexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author chaokunyang
 */
@RestController
@RequestMapping("/api/search")
public class SearchEndpoint {

    private Indexer indexer;

    @Autowired
    public SearchEndpoint(Indexer indexer) {
        this.indexer = indexer;
    }

    @PostMapping("/start_all")
    public void startAll() {
        indexer.startAll();
    }

    @RequestMapping(value = "/start/{entityClassName:.+}", method = RequestMethod.POST)
    public void start(@PathVariable("entityClassName") String entityClassName) {
        indexer.start(entityClassName);
    }

    @PutMapping("stop_all")
    public Map<String, Boolean> stopAll() {
        return indexer.stopAll();
    }

    @RequestMapping(value = "/stop/{entityClassName:.+}", method = RequestMethod.PUT)
    public Boolean stop(@PathVariable("entityClassName") String entityClassName) {
        return indexer.stop(entityClassName);
    }

    @GetMapping("progress")
    public Map<String, IndexProgress> getProgress() {
        return indexer.getProgress();
    }

}
