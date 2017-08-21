package com.timeyang.jkes.spring.jpa;

import com.timeyang.jkes.SearchPackage;
import com.timeyang.jkes.spring.jpa.intercept.OperationAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author chaokunyang
 */
@Configuration
@ComponentScan(basePackageClasses = SearchPackage.class)
public class JkesSpringConfig {
    // declare aspect
    @Bean
    @Order(1)
    public OperationAspect operationAspect(EventSupport eventSupport) {
        return new OperationAspect(eventSupport);
    }
}
