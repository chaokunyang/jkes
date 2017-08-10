package com.timeyang.jkes.integration_test.domain;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import com.timeyang.jkes.core.annotation.InnerField;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.spring.jpa.audit.AuditedEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author chaokunyang
 */
@Setter
@ToString(exclude = "personGroup")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Document
public class Person extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender;
    private Integer age;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private PersonGroup personGroup;

    @Field(type = FieldType.Long)
    public Long getId() {
        return id;
    }

    @MultiFields(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
            }
    )
    public String getName() {
        return name;
    }

    @Field(type = FieldType.Keyword)
    public String getGender() {
        return gender;
    }

    @Field(type = FieldType.Integer)
    public Integer getAge() {
        return age;
    }

    /**
     * don't add @Field to test whether ignored
     */
    // @Field(type = FieldType.Text)
    public String getDescription() {
        return description;
    }

    @Field(type = FieldType.Object)
    public PersonGroup getPersonGroup() {
        return personGroup;
    }
}
