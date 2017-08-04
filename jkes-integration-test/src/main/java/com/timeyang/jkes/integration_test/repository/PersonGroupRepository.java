package com.timeyang.jkes.integration_test.repository;

import com.timeyang.jkes.integration_test.domain.PersonGroup;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author chaokunyang
 */
public interface PersonGroupRepository extends PagingAndSortingRepository<PersonGroup, Long> {
}
