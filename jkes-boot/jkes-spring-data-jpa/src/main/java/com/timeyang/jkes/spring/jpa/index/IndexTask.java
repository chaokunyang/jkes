package com.timeyang.jkes.spring.jpa.index;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Note the map is unmodifiable
 * <p>key: entity class canonicalName. <br/>
 * value: index progress</p>
 * @return progress map
 */

public interface IndexTask<T> {

    Class<T> getDomainClass();

    long count();

    Page<T> getData(Pageable pageable);
}
