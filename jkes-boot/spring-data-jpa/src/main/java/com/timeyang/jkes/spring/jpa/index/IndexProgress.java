package com.timeyang.jkes.spring.jpa.index;

import lombok.Getter;

/**
 * Index Progress
 * <p>Notes It's immutable, so concurrent access is safe</p>
 * @author chaokunyang
 */
@Getter
public class IndexProgress {

    private final Class<?> clazz;
    private final Long total;
    private final Long indexed;
    private final double rate;

    public IndexProgress(Class<?> clazz, Long total, Long indexed) {
        this.clazz = clazz;
        this.total = total;
        this.indexed = indexed;
        this.rate = (double)indexed / (double)total;
    }
}
