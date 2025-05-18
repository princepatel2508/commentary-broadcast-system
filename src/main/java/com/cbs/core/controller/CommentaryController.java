package com.cbs.core.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cbs.core.model.CommentaryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/commentary")
public class CommentaryController {

	private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public CommentaryController(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            RedisTemplate<String, String> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public ResponseEntity<String> postCommentary(@RequestBody CommentaryDTO dto) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(dto);
        kafkaTemplate.send("commentary-topic", dto.getMatchId(), message);
        return ResponseEntity.ok("Sent to Kafka"+dto.getMatchId());
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<CommentaryDTO>> getCommentaryAfter(
            @RequestParam String matchId,
            @RequestParam long after) throws JsonProcessingException {
        String redisKey = "match:" + matchId + ":commentary";
        Set<String> results = redisTemplate.opsForZSet()
                .rangeByScore(redisKey, after + 1, Double.MAX_VALUE);
        List<CommentaryDTO> commentaryList = new ArrayList<>();
        if (results != null) {
            for (String json : results) {
                CommentaryDTO dto = objectMapper.readValue(json, CommentaryDTO.class);
                commentaryList.add(dto);
            }
        }
        commentaryList.sort(Comparator.comparingLong(CommentaryDTO::getTimestamp));
        return ResponseEntity.ok(commentaryList);
    }

}

