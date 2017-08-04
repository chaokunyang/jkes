package com.timeyang.jkes.entity;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * @author chaokunyang
 */
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PhoneNumber {
    private String countryCode;
    private String number;

    @Field(type = FieldType.Keyword)
    public String getCountryCode() {
        return countryCode;
    }

    @Field(type = FieldType.Keyword)
    public String getNumber() {
        return number;
    }
}
