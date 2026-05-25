package com.aiinterview.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Backend is running successfully!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test api is working!";
    }
}