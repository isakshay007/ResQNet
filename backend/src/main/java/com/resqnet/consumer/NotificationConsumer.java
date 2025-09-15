package com.resqnet.consumer;

import com.resqnet.dto.NotificationDTO;
import com.resqnet.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Consume notifications from Kafka and persist in DB
     */
    @KafkaListener(
            topics = "notifications",
            groupId = "resqnet-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(ConsumerRecord<String, NotificationDTO> record) {
        NotificationDTO dto = record.value();

        try {
            log.info(" Consuming notification from Kafka: {}", dto);
            notificationService.saveNotification(dto);
            log.info("Notification persisted successfully [id={}, type={}]", dto.getId(), dto.getType());
        } catch (Exception e) {
            log.error(" Failed to process notification from Kafka. Message: {}, Partition: {}, Offset: {}",
                    dto, record.partition(), record.offset(), e);

            // Optionally: send to dead-letter topic, or handle retries
            // deadLetterProducer.send(record);
        }
    }
}
