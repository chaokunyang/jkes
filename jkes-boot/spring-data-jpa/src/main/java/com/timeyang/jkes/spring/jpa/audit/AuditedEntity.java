package com.timeyang.jkes.spring.jpa.audit;

import com.timeyang.jkes.core.annotation.Field;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Audited Entity
 *
 * @author chaokunyang
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class AuditedEntity {

    @CreatedDate // Created Date
    @Column(nullable = false)
    @Field
    private Date dateCreated;

    @LastModifiedDate // Last Modified Date
    @Column(nullable = false)
    @Field
    private Date dateModified;

    @javax.persistence.Version
    @Column(name = "revision")
    @Field
    private long version;

}
