package com.example.sample.controller;

import com.example.sample.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducerService producerService;

    @PostMapping("/publish")
    public String publish(@RequestParam Long userId, @RequestParam String action) {
        producerService.sendMessage(userId, action);
        return "Message published";
    }
}
