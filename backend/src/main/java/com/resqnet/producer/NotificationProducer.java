package com.resqnet.producer;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {

    private static final String TOPIC = "notifications";
    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, NotificationDTO> kafkaTemplate;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationProducer(
            @Autowired(required = false) KafkaTemplate<String, NotificationDTO> kafkaTemplate,
            NotificationService notificationService,
            @Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;

        if (kafkaTemplate != null) {
            log.info("Kafka available — notifications will be dispatched via Kafka");
        } else {
            log.info("Kafka unavailable — notifications will be dispatched synchronously");
        }
    }

    public void sendNotification(NotificationDTO notification) {
        if (kafkaTemplate != null) {
            kafkaTemplate.send(TOPIC, notification);
        } else {
            dispatchDirectly(notification);
        }
    }

    private void dispatchDirectly(NotificationDTO notification) {
        try {
            NotificationDTO saved = notificationService.saveNotification(notification);
            pushViaWebSocket(saved);
        } catch (Exception e) {
            log.error("Failed to dispatch notification directly: {}", e.getMessage(), e);
        }
    }

    private void pushViaWebSocket(NotificationDTO saved) {
        if (messagingTemplate == null) return;
        try {
            if (saved.isAdminBroadcast()) {
                messagingTemplate.convertAndSend("/topic/notifications/admin", saved);
            }
            String email = saved.getRecipientEmail();
            if (email != null && !"ADMIN-BROADCAST".equals(email)) {
                messagingTemplate.convertAndSend("/queue/notifications/" + email, saved);
            }
        } catch (Exception e) {
            log.warn("WebSocket push failed (non-fatal): {}", e.getMessage());
        }
    }
}
