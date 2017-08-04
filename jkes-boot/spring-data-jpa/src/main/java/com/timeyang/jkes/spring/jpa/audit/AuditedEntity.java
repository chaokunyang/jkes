package com.timeyang.jkes.spring.jpa.audit;

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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
