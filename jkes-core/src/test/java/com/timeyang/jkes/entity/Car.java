package com.timeyang.jkes.entity;

import lombok.AllArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * @author chaokunyang
 */
@Embeddable
@Setter
@AllArgsConstructor
public class Car {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
