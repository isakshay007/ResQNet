package com.resqnet.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DisasterProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "disaster-reports";

    public DisasterProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("📡 Sent message to Kafka: " + message);
    }
}
