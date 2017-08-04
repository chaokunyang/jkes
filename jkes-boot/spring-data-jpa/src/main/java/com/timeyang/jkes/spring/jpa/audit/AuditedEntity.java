package com.timeyang.jkes.spring.jpa.audit;

import com.timeyang.jkes.core.annotation.DocumentVersion;
import com.timeyang.jkes.core.annotation.Field;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.Date;

/**
 * Audited Entity
 *
 * @author chaokunyang
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity {

    @CreatedDate // Created Date
    @Column(nullable = false)
    private Date dateCreated;

    @LastModifiedDate // Last Modified Date
    @Column(nullable = false)
    private Date dateModified;

    @Version
    @Column(name = "revision")
    private long version;

    @Field
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Field
    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Field
    @DocumentVersion
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
