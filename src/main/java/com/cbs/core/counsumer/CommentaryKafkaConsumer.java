package com.cbs.core.counsumer;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.cbs.core.model.CommentaryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommentaryKafkaConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CommentaryKafkaConsumer(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "commentary-topic", groupId = "cbs-group")
    public void listen(String message) throws JsonProcessingException {
        CommentaryDTO dto = objectMapper.readValue(message, CommentaryDTO.class);
        String key = "match:" + dto.getMatchId() + ":commentary";
        System.out.println("Reached "+ key);
        redisTemplate.opsForZSet().add(key, message, dto.getTimestamp());
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }
}
