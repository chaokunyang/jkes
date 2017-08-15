package com.timeyang.jkes.integration_test.config;

import com.timeyang.jkes.integration_test.Application;
import com.timeyang.jkes.spring.jpa.EnableJkes;
import com.timeyang.jkes.spring.jpa.EventSupport;
import com.timeyang.jkes.spring.jpa.SearchPlatformTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
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
@EnableJpaAuditing
@EnableJkes
public class SearchConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory factory, EventSupport eventSupport) {

        return new SearchPlatformTransactionManager(new JpaTransactionManager(factory), eventSupport);
    }
}
