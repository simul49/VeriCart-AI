package com.vericart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "app", "VeriCart AI - Trust-Driven Commerce",
            "status", "running",
            "time", LocalDateTime.now().toString()
        );
    }
}
