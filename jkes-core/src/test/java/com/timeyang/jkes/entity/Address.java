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
public class Address {
    private String street;
    private String city;
    private String state;
    private String country;

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }
}