package com.timeyang.jkes.entity;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.annotation.DocumentId;
import com.timeyang.jkes.core.annotation.Field;
import com.timeyang.jkes.core.annotation.FieldType;
import com.timeyang.jkes.core.annotation.InnerField;
import com.timeyang.jkes.core.annotation.MultiFields;
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
public class PersonGroup {
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
     * 不加Field注解，测试序列化时是否忽略
     */
    public String getDescription() {
        return description;
    }
}
