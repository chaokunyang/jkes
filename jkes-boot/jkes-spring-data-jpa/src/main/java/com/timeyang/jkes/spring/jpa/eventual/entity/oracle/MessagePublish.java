package com.timeyang.jkes.spring.jpa.eventual.entity.oracle;

import com.timeyang.jkes.spring.jpa.audit.AuditedEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * 用于记录待发送数据到数据库，避免在事务完成之后消息没有发送到Kafka
 * <p>
 *     在后台以定时调度的方式轮询该表，从该表获取信息，然后查询相关数据并发送到Kafka，当相应数据成功发送到Kafka后，删除该表相应记录。
 * </p>
 *
 * <p>
 *     为了避免在生产者正常工作的情况下发送数据，后台定时调度可设为每60s查询一次时间在2分钟以前的数据进行发送。这样可以避免后台调度任务重复发送生产者已经发送过的数据，因为2分钟足够Kafka Producer完成数据发送，并且从数据库删除相应的消息ID记录.
 * </p>
 *
 * <p>
 *     为了避免频繁的的数据库删除操作，可以缓存已完成的消息记录，批量删除，注意该时间必须小于之前的2分钟
 * </p>
 *
 * <p>
 *     以继承的方式可能会影响客户端实体的层次结构，最好还是使用反射在实体中lookup消息记录字段，然后填充相应数据
 * </p>
 * @author chaokunyang
 */
@Entity(name = "MessagePublish")
@Table(name = "kafka_message_publish")
public class MessagePublish extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kafka_message_publish_gen")
    @SequenceGenerator(name = "kafka_message_publish_gen", sequenceName = "kafka_message_publish_gen", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    private Long messageId;

    private Date createdDate;

    /**
     * 该时刻之前的消息已经发生完成
     */
    private Date publishedBefore;

    /**
     * 重新索引时会指定新的topic，根据该topic查询待发送的数据
     */
    private String topic;

    /**
     * 消息实体完全限定类名，用于从，然后根据messageId获取待发送的数据
     */
    private String messageEntityClassName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}