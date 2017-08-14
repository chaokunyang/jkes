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

    // @Id will be identified automatically
    // @Field(type = FieldType.Long)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MultiFields(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
            }
    )
    private String name;

    @Field(type = FieldType.Keyword)
    private String gender;

    @Field(type = FieldType.Integer)
    private Integer age;

    // don't add @Field to test whether ignored
    // @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Object)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private PersonGroup personGroup;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }

    public PersonGroup getPersonGroup() {
        return personGroup;
    }
}
