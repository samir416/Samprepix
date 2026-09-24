package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.PaymentRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        User user = getAuthenticatedUser(authentication);

        try {
            return ResponseEntity.ok(
                    paymentService.createOrder(user, request, false)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to create payment order"
                    ));
        }
    }

    @PostMapping("/create-test-order")
    public ResponseEntity<?> createTestOrder(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        User user = getAuthenticatedUser(authentication);

        try {
            return ResponseEntity.ok(
                    paymentService.createOrder(user, request, true)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to create test payment order"
                    ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            Authentication authentication,
            @RequestBody Map<String, String> payload) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment verification payload is required"));
        }

        String orderId = payload.get("order_id");

        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing order_id"));
        }

        orderId = orderId.trim();

        if (!isSafeOrderId(orderId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid order_id"));
        }

        User user = getAuthenticatedUser(authentication);

        try {
            Map<String, Object> result =
                    paymentService.verifyAndActivatePayment(
                            orderId,
                            user.getId()
                    );

            String status = result.get("status") != null
                    ? String.valueOf(result.get("status"))
                    : "";

            if ("pending".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(result);
            }

            if ("failed".equalsIgnoreCase(status)) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(result);
            }

            if ("success".equalsIgnoreCase(status)
                    || "paid".equalsIgnoreCase(status)
                    || "completed".equalsIgnoreCase(status)
                    || "already_processed".equalsIgnoreCase(status)
                    || "already_success".equalsIgnoreCase(status)) {
                return ResponseEntity.ok(result);
            }

            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (PaymentService.PaymentFailedException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of(
                            "status", "failed",
                            "error", e.getMessage()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Payment verification failed"
                    ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(
                    value = "x-webhook-signature",
                    required = false
            ) String signature,
            @RequestHeader(
                    value = "x-webhook-timestamp",
                    required = false
            ) String timestamp,
            @RequestBody String rawBody) {

        if (signature == null || signature.isBlank()
                || timestamp == null || timestamp.isBlank()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error",
                            "Missing webhook signature or timestamp"
                    ));
        }

        if (rawBody == null || rawBody.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Empty webhook payload"));
        }

        if (rawBody.length() > 1_000_000) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Webhook payload is too large"));
        }

        if (signature.length() > 4096 || timestamp.length() > 128) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook signature data"));
        }

        try {
            return ResponseEntity.ok(
                    paymentService.handleWebhook(
                            signature.trim(),
                            timestamp.trim(),
                            rawBody
                    )
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Webhook processing failed"
                    ));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                paymentRepository.findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
        );
    }

    @PostMapping("/mark-failed")
    public ResponseEntity<?> markPaymentFailed(
            Authentication authentication,
            @RequestBody Map<String, String> payload) {

        User user = getAuthenticatedUser(authentication);

        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment failure payload is required"));
        }

        String orderId = payload.get("order_id");

        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing order_id"));
        }

        orderId = orderId.trim();

        if (!isSafeOrderId(orderId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid order_id"));
        }

        try {
            paymentService.markPaymentFailed(
                    orderId,
                    user.getId()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "status",
                            "failed",
                            "message",
                            "Payment marked as failed"
                    )
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));

        } catch (PaymentService.PaymentFailedException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of(
                            "status", "failed",
                            "error", e.getMessage()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to mark payment as failed"
                    ));
        }
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new SecurityException("Unauthorized");
        }

        String email = authentication.getName().trim();

        if (email.length() > 254
                || email.indexOf('\n') >= 0
                || email.indexOf('\r') >= 0) {
            throw new SecurityException("Invalid authentication identity");
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new SecurityException(
                                "Authenticated user not found"
                        )
                );
    }

    private boolean isSafeOrderId(String orderId) {
        return orderId.length() <= 100
                && orderId.matches("[A-Za-z0-9_-]+");
    }
}