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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EntitlementService entitlementService;

    // =========================================================
    // BASIC HEALTH CHECK
    // =========================================================

    @GetMapping("/")
    public String home() {
        return "Backend is running successfully!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test api is working!";
    }

    // =========================================================
    // AUTHENTICATION
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest) {

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

        return ResponseEntity.ok(new LoginResponse(response));
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

        String response = userService.saveUser(user);

        if (response.equals("Email already exists!")
                || response.equals("Username already exists!")) {

            return ResponseEntity
                    .badRequest()
                    .body(new RegisterResponse(
                            false,
                            response,
                            null
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegisterResponse(
                        true,
                        response,
                        user.getEmail()
                ));
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
                    .body(new ApiResponse(false, response));
        }

        return ResponseEntity.ok(
                new ApiResponse(true, response)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String response = userService.forgotPassword(
                request.getEmail()
        );

        if (response.equals("Email not found!")) {

            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, response));
        }

        return ResponseEntity.ok(
                new ApiResponse(true, response)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String response = userService.resetPassword(
                request.getToken(),
                request.getPassword()
        );

        if (!response.equals("Password reset successfully!")) {

            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, response));
        }

        return ResponseEntity.ok(
                new ApiResponse(true, response)
        );
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized");
        }

        UserResponse user = userService.getCurrentUser(
                authentication.getName()
        );

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found!");
        }

        userRepository.findByEmail(authentication.getName())
                .ifPresent(currentUser ->
                        user.setPlan(
                                entitlementService.getEffectivePlan(
                                        currentUser
                                )
                        )
                );

        return ResponseEntity.ok(user);
    }

    // =========================================================
    // LEGACY USER MANAGEMENT
    // Keep restricted to ADMIN.
    // Actual admin panel uses /api/admin/**.
    // =========================================================

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id) {

        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        Optional<User> existingUser =
                userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existingUser.get();

        if (updatedUser.getUsername() != null
                && !updatedUser.getUsername().isBlank()) {

            user.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getEmail() != null
                && !updatedUser.getEmail().isBlank()) {

            user.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            user.setPassword(updatedUser.getPassword());
        }

        return ResponseEntity.ok(
                userRepository.save(user)
        );
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

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

    // =========================================================
    // TEST ENDPOINT
    // =========================================================

    @PostMapping("/login-test")
    public String loginTest() {
        return "working";
    }

    // =========================================================
    // PROFILE TEST
    // =========================================================

    @GetMapping("/profile")
    public String profile() {
        return "Profile Access Granted";
    }
}