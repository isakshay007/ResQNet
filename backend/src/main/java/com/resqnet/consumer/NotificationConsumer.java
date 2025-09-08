package com.resqnet.consumer;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Consume notifications from Kafka and persist in DB
    @KafkaListener(topics = "notifications", groupId = "resqnet-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotification(NotificationDTO dto) {
        // Store in DB
        notificationService.saveNotification(dto);
    }
}
