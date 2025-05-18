package com.cbs.core.service;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cbs.core.model.CommentaryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CommentaryBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CommentaryBroadcaster(SimpMessagingTemplate messagingTemplate,
                                 RedisTemplate<String, String> redisTemplate,
                                 ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 1000)
    public void broadcast() throws JsonProcessingException {
        Set<String> keys = redisTemplate.keys("match:*:commentary");
        if (keys != null) {
            for (String key : keys) {
                Set<String> messages = redisTemplate.opsForZSet().range(key, 0, -1);
                if (messages != null) {
                    for (String message : messages) {
                        CommentaryDTO dto = objectMapper.readValue(message, CommentaryDTO.class);
                        messagingTemplate.convertAndSend("/topic/match/" + dto.getMatchId(), dto);
                    }
                    //redisTemplate.delete(key); -- No need to delete now, will manage the data via (history API + timestamp)
                }
            }
        }
    }
}
