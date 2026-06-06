package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.model.LoginRequest;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.aiinterview.backend.security.JwtUtil;
import com.aiinterview.backend.model.LoginResponse;
import jakarta.validation.Valid;
import com.aiinterview.backend.model.RegisterRequest;
import com.aiinterview.backend.model.ApiResponse;


@RestController
public class TestController {

    private final UserRepository userRepository;
    private final UserService userService;

    public TestController(
            UserRepository userRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
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
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest) {

        String response = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword());

        if (response.equals("Invalid password!")
                || response.equals("User not found!")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(
                new LoginResponse(response));
    }

 @PostMapping("/register")
public ResponseEntity<ApiResponse> register(
        @Valid
        @RequestBody
        RegisterRequest request) {

    User user = new User();

    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(request.getPassword());

    String response =
            userService.saveUser(user);

    if (response.equals(
            "Email already exists!")) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse(
                                false,
                                response
                        )
                );
    }

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                    new ApiResponse(
                            true,
                            response
                    )
            );
}


    @GetMapping("/users")
    public List<User> getUsers() {

        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Long id) {

        Optional<User> user = userRepository.findById(id);

        return user.orElse(null);
    }

    @PutMapping("/users/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {

            User user = existingUser.get();

            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());

            return userRepository.save(user);
        }

        return null;
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        if (userRepository.existsById(id)) {

            userRepository.deleteById(id);

            return "User deleted successfully!";
        }

        return "User not found!";
    }

    @PostMapping("/login-test")
    public String loginTest() {
        return "working";
    }

  @GetMapping("/profile")
public String profile() {

    return "Profile Access Granted";
}
}