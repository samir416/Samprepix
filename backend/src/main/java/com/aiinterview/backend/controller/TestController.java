package com.aiinterview.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.aiinterview.backend.model.LoginRequest;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

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

    @GetMapping("/users")
    public List <User>getUsers()
    {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            user.setName(updatedUser.getName());
            return userRepository.save(user);
        } else {
            return null;
        }
    }

}