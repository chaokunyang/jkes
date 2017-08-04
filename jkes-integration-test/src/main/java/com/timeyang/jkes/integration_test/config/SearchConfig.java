package com.timeyang.jkes.integration_test.config;

import com.timeyang.jkes.integration_test.Application;
import com.timeyang.jkes.spring.jpa.EventSupport;
import com.timeyang.jkes.spring.jpa.JkesSpringConfig;
import com.timeyang.jkes.spring.jpa.SearchPlatformTransactionManager;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

/**
 * @author chaokunyang
 */
@Configuration
@ComponentScans({
    @ComponentScan(basePackageClasses = Application.class)
})
@EnableAspectJAutoProxy
@Import(JkesSpringConfig.class)
public class SearchConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory factory, EventSupport eventSupport) {

        return new SearchPlatformTransactionManager(new JpaTransactionManager(factory), eventSupport);
    }
}
