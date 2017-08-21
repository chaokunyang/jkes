package com.timeyang.jkes.spring.jpa.eventual.schedule;

import com.timeyang.jkes.spring.jpa.eventual.entity.oracle.MessagePublish;
import com.timeyang.jkes.core.kafka.producer.JkesKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * 事件发送服务
 *
 * @author chaokunyang
 */
// @Service
public class MessagePublishService {

    // private ConcurrentMap<String, Method>

    @Autowired
    private MessagePublishRepository messagePublishRepository;

    @Autowired
    private JkesKafkaProducer producerClient;

    public void deletePublishedMessageRecord() {

    }

    @Scheduled(fixedDelay = 120_000L)
    public void publishMessage() {
        long messagesToPublish = messagePublishRepository.count();
        for(int i = 0; i <= messagesToPublish / 100; i++) {
            publishAndDelete();
        }
    }

    @Transactional
    public void publishAndDelete() {
        Page<MessagePublish> page = messagePublishRepository.findAll(new PageRequest(0, 100));

        // Method method = (new MessagePublishService(){}).publishMessage();

        for (MessagePublish messagePublish : page) {

        }
    }

}
