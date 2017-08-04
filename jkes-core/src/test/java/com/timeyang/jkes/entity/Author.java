package com.timeyang.jkes.entity;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author chaokunyang
 */
@NoArgsConstructor
@Setter
public class Author {

    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}