package com.example.urlshortener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CLICK_EVENTS_TOPIC = "url-click-events";

    /**
     * Send click event to Kafka
     */
    public void sendClickEvent(ClickEvent clickEvent) {
        try {
            String message = objectMapper.writeValueAsString(clickEvent);
            kafkaTemplate.send(CLICK_EVENTS_TOPIC, clickEvent.getShortCode(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize click event", e);
        }
    }
}

