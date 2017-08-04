package com.timeyang.jkes.services.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;

/**
 * @author chaokunyang
 */
@Configuration
public class WebConfig {

    @Bean
    public HttpMessageConverters customConverters() {
        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(Arrays.asList(
                new MediaType("application", "json"),
                new MediaType("text", "json")
        ));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JsonOrgModule());
        jsonConverter.setObjectMapper(objectMapper);
        return new HttpMessageConverters(jsonConverter);
    }

}
