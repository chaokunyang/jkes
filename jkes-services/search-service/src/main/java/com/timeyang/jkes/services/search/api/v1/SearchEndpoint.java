package com.timeyang.jkes.services.search.api.v1;

import com.timeyang.jkes.services.search.domain.SearchResponseProjection;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chaokunyang
 */
@RestController
@RequestMapping("/api/v1")
public class SearchEndpoint {

    private SearchService searchService;

    @Autowired
    public SearchEndpoint(SearchService searchService) {
        this.searchService = searchService;
    }

    @RequestMapping(value = "{index}/{type}/{id}", method = RequestMethod.GET)
    public JSONObject get(@PathVariable("index") String index,
                                   @PathVariable("type") String type,
                                   @PathVariable("id") String id) {
        return this.searchService.get(index, type, id).get_source();
    }

    @RequestMapping(value = "{index}/{type}/_search", method = {RequestMethod.GET, RequestMethod.POST})
    public SearchResponseProjection search(@PathVariable("index") String index,
                                           @PathVariable("type") String type,
                                           @RequestBody(required = false) JSONObject request, Pageable pageable, HttpServletRequest req) {
        return this.searchService.search(index, type, request, req.getQueryString());
    }

}
