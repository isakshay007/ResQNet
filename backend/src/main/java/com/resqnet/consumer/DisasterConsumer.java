package com.resqnet.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DisasterConsumer {

    @KafkaListener(topics = "disaster-reports", groupId = "resqnet-group")
    public void consume(String message) {
        System.out.println("📥 Consumed message from Kafka: " + message);
    }
}
