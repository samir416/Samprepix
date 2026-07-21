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
import com.aiinterview.backend.model.LoginResponse;
import jakarta.validation.Valid;
import com.aiinterview.backend.model.RegisterRequest;
import com.aiinterview.backend.model.ApiResponse;
import com.aiinterview.backend.model.ForgotPasswordRequest;
import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.model.ResetPasswordRequest;

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
                        @Valid @RequestBody RegisterRequest request) {

                User user = new User();

                user.setUsername(request.getUsername());
                user.setName(request.getUsername()); // Temporary compatibility
                user.setEmail(request.getEmail());
                user.setPassword(request.getPassword());
                user.setProvider(AuthenticationProvider.EMAIL);

                String response = userService.saveUser(user);

                if (response.equals("Email already exists!")
                                || response.equals("Username already exists!")) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(new ApiResponse(false, response));
                }

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(new ApiResponse(true, response));
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request) {

                String response = userService.forgotPassword(
                                request.getEmail());

                if (response.equals("Email not found!")) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(new ApiResponse(
                                                        false,
                                                        response));
                }

                return ResponseEntity.ok(
                                new ApiResponse(
                                                true,
                                                response));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {

                String response = userService.resetPassword(
                                request.getToken(),
                                request.getPassword());

                if (!response.equals("Password reset successfully!")) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(new ApiResponse(
                                                        false,
                                                        response));
                }

                return ResponseEntity.ok(
                                new ApiResponse(
                                                true,
                                                response));
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

                        user.setUsername(updatedUser.getUsername());
                        user.setEmail(updatedUser.getEmail());

                        if (updatedUser.getPassword() != null
                                        && !updatedUser.getPassword().isBlank()) {

                                user.setPassword(updatedUser.getPassword());
                        }

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