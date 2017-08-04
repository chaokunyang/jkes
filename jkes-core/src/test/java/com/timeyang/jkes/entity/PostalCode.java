package com.timeyang.jkes.entity;

import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import lombok.AllArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * @author chaokunyang
 */
@Setter
@AllArgsConstructor
@Embeddable
public class PostalCode {
    private String code;
    private String suffix;

    @Field(type = FieldType.Keyword)
    public String getCode() {
        return code;
    }

    @Field(type = FieldType.Keyword)
    public String getSuffix() {
        return suffix;
    }
}
