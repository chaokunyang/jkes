package com.timeyang.jkes.integration_test.repository;

import com.timeyang.jkes.integration_test.domain.Person;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author chaokunyang
 */
public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {
}
