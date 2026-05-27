package com.aiinterview.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.aiinterview.backend.model.LoginRequest;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.entity.User;

@RestController
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "Backend is running successfully!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test api is working!";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        return "Hello, " + loginRequest.getEmail() + "!";
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        userRepository.save(user);

        return "User saved successfully!";
    }

}