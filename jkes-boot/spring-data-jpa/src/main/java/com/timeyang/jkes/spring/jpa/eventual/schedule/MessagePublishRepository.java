package com.timeyang.jkes.spring.jpa.eventual.schedule;

import com.timeyang.jkes.spring.jpa.eventual.entity.oracle.MessagePublish;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

/**
 * @author chaokunyang
 */
public interface MessagePublishRepository extends PagingAndSortingRepository<MessagePublish, Long> {

    List<MessagePublish> findByCreatedDateLessThan(Date date);

}
