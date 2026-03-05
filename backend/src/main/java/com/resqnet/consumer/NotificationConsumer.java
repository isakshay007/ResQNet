package com.resqnet.consumer;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(NotificationService notificationService,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "notifications",
            groupId = "resqnet-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(ConsumerRecord<String, NotificationDTO> record) {
        NotificationDTO dto = record.value();

        try {
            log.info("Consuming notification from Kafka: {}", dto);
            NotificationDTO saved = notificationService.saveNotification(dto);
            log.info("Notification persisted successfully [id={}, type={}]", saved.getId(), saved.getType());

            pushViaWebSocket(saved);
        } catch (Exception e) {
            log.error("Failed to process notification from Kafka. Message: {}, Partition: {}, Offset: {}",
                    dto, record.partition(), record.offset(), e);
        }
    }

    private void pushViaWebSocket(NotificationDTO saved) {
        try {
            if (saved.isAdminBroadcast()) {
                messagingTemplate.convertAndSend("/topic/notifications/admin", saved);
                log.debug("WebSocket push to /topic/notifications/admin");
            }

            String email = saved.getRecipientEmail();
            if (email != null && !"ADMIN-BROADCAST".equals(email)) {
                messagingTemplate.convertAndSend("/queue/notifications/" + email, saved);
                log.debug("WebSocket push to /queue/notifications/{}", email);
            }
        } catch (Exception e) {
            log.warn("WebSocket push failed (non-fatal): {}", e.getMessage());
        }
    }
}
