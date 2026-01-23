package com.example.sample.controller;

import com.example.sample.service.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메시지 publish API(kafka)", description = "메시지 kafka publish")
@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaProducerService producerService;

    @Operation(summary = "메시지 publish", description = "메시지를 publish 합니다.")
    @PostMapping("/publish")
    public String publish(@RequestParam Long userId, @RequestParam String action) {
        producerService.sendMessage(userId, action);
        return "Message published";
    }

}
