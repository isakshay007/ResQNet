package com.resqnet.producer;

import com.resqnet.dto.NotificationDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {

    private static final String TOPIC = "notifications"; // Kafka topic

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, NotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //  Publish a notification event
    public void sendNotification(NotificationDTO notification) {
        kafkaTemplate.send(TOPIC, notification);
    }
}
