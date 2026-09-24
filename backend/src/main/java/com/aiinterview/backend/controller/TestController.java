package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.AuthenticationProvider;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.model.ApiResponse;
import com.aiinterview.backend.model.ForgotPasswordRequest;
import com.aiinterview.backend.model.LoginRequest;
import com.aiinterview.backend.model.LoginResponse;
import com.aiinterview.backend.model.RegisterRequest;
import com.aiinterview.backend.model.RegisterResponse;
import com.aiinterview.backend.model.ResendOtpRequest;
import com.aiinterview.backend.model.ResetPasswordRequest;
import com.aiinterview.backend.model.UserResponse;
import com.aiinterview.backend.model.VerifyOtpRequest;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.security.JwtUtil;
import com.aiinterview.backend.service.EntitlementService;
import com.aiinterview.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EntitlementService entitlementService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        String response = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (response.equals("Invalid password!")
                || response.equals("User not found!")
                || response.equals("Please verify your email first!")
                || response.equals("Account is not active!")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(
                new LoginResponse(response)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setProvider(AuthenticationProvider.EMAIL);

        RegisterResponse response =
                userService.saveUser(user);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        String response = userService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        switch (response) {
            case "User not found!",
            "OTP not found!",
            "OTP has already been used!",
            "OTP has expired!",
            "Invalid OTP!" -> {

                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(
                                false,
                                response
                        ));
            }

            default -> {
                return ResponseEntity.ok(
                        new LoginResponse(response)
                );
            }
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        String response = userService.resendOtp(
                request.getEmail()
        );

        if (response.equals("User not found!")
                || response.equals("Email is already verified!")) {

            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(
                            false,
                            response
                    ));
        }

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        response
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String response =
                userService.forgotPassword(
                        request.getEmail()
                );

        if (response.equals("Email not found!")) {

            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(
                            false,
                            response
                    ));
        }

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        response
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String response =
                userService.resetPassword(
                        request.getToken(),
                        request.getPassword()
                );

        if (!response.equals(
                "Password reset successfully!"
        )) {

            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(
                            false,
                            response
                    ));
        }

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        response
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized");
        }

        String email = authentication.getName().trim();

        if (email.length() > 254
                || email.contains("\r")
                || email.contains("\n")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized");
        }

        UserResponse user =
                userService.getCurrentUser(email);

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found!");
        }

        userRepository.findByEmail(email)
                .ifPresent(currentUser ->
                        user.setPlan(
                                entitlementService
                                        .getEffectivePlan(
                                                currentUser
                                        )
                        )
                );

        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        if (id == null
                || id <= 0
                || updatedUser == null) {

            return ResponseEntity.badRequest().build();
        }

        return userRepository.findById(id)
                .map(user -> {

                    if (updatedUser.getUsername() != null
                            && !updatedUser.getUsername().isBlank()) {

                        String username =
                                updatedUser.getUsername().trim();

                        if (username.length() <= 50
                                && !user.getUsername()
                                .equals(username)
                                && userRepository
                                .existsByUsername(username)) {

                            return null;
                        }

                        if (username.length() <= 50) {
                            user.setUsername(username);
                        }
                    }

                    if (updatedUser.getEmail() != null
                            && !updatedUser.getEmail().isBlank()) {

                        String email =
                                updatedUser.getEmail().trim();

                        if (email.length() <= 254
                                && !user.getEmail()
                                .equalsIgnoreCase(email)
                                && userRepository
                                .existsByEmail(email)) {

                            return null;
                        }

                        if (email.length() <= 254) {
                            user.setEmail(email);
                        }
                    }

                    if (updatedUser.getName() != null
                            && updatedUser.getName().length() <= 100) {

                        user.setName(
                                updatedUser.getName().trim()
                        );
                    }

                    return userRepository.save(user);
                })
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity
                                .badRequest()
                                .build()
                );
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid user ID.");
        }

        if (!userRepository.existsById(id)) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found!");
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok(
                "User deleted successfully!"
        );
    }
}