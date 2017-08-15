package com.timeyang.jkes.integration_test.domain;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.DocumentId;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import com.timeyang.jkes.core.annotation.InnerField;
import com.timeyang.jkes.core.annotation.MultiFields;
import com.timeyang.jkes.spring.jpa.audit.AuditedEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

/**
 * @author chaokunyang
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Document(type = "person_group", alias = "person_group_alias")
public class PersonGroup extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String interests;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "personGroup", orphanRemoval = true)
    private List<Person> persons;
    private String description;

    @DocumentId
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

    @Field(type = FieldType.Text)
    public String getInterests() {
        return interests;
    }

    @Field(type = FieldType.Nested)
    public List<Person> getPersons() {
        return persons;
    }

    /**
     * don't add @Field to test whether ignored
     */
    public String getDescription() {
        return description;
    }
}

