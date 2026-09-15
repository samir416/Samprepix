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

    // =========================================================
    // CREATE PRODUCTION ORDER
    // =========================================================

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        User user = getAuthenticatedUser(authentication);

        try {
            Map<String, Object> result =
                    paymentService.createOrder(
                            user,
                            request,
                            false
                    );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity.status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to create payment order"
                    ));
        }
    }

    // =========================================================
    // CREATE TEST ORDER
    // =========================================================

    @PostMapping("/create-test-order")
    public ResponseEntity<?> createTestOrder(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        User user = getAuthenticatedUser(authentication);

        try {
            Map<String, Object> result =
                    paymentService.createOrder(
                            user,
                            request,
                            true
                    );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity.status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Unable to create test payment order"
                    ));
        }
    }

    // =========================================================
    // VERIFY PAYMENT
    // =========================================================

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            Authentication authentication,
            @RequestBody Map<String, String> payload) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        String orderId = payload.get("order_id");
        if (orderId == null || orderId.isBlank()) {
            // Check legacy razorpay key just in case, but rely on order_id
            orderId = payload.get("razorpay_order_id");
        }

        if (isBlank(orderId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing order_id"));
        }

        User user = getAuthenticatedUser(authentication);

        try {
            Map<String, Object> result = paymentService.verifyAndActivatePayment(orderId.trim(), user.getId());
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Payment verification failed"));
        }
    }

    // =========================================================
    // CASHFREE WEBHOOK
    // =========================================================

    /**
     * Cashfree calls this endpoint directly.
     *
     * This endpoint does NOT use normal JWT authentication.
     * Security is handled by PaymentService using the Cashfree
     * webhook signature.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(value = "x-webhook-signature", required = false) String signature,
            @RequestHeader(value = "x-webhook-timestamp", required = false) String timestamp,
            @RequestBody String rawBody) {

        if (isBlank(signature) || isBlank(timestamp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing webhook signature or timestamp"));
        }

        if (rawBody == null || rawBody.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty webhook payload"));
        }

        try {
            Map<String, Object> result = paymentService.handleWebhook(signature.trim(), timestamp.trim(), rawBody);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Webhook processing failed"));
        }
    }

    // =========================================================
    // PAYMENT HISTORY
    // =========================================================

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                paymentRepository.findByUserId(
                        user.getId()
                )
        );
    }

    // =========================================================
    // MARK PAYMENT FAILED
    // =========================================================

    @PostMapping("/mark-failed")
    public ResponseEntity<?> markFailed(
            Authentication authentication,
            @RequestBody Map<String, String> payload) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity.status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(Map.of(
                            "error",
                            "Unauthorized"
                    ));
        }

        String orderId = payload.get("order_id");
        if (orderId == null || orderId.isBlank()) {
            orderId = payload.get("razorpay_order_id");
        }

        if (isBlank(orderId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing order_id"));
        }

        User user = getAuthenticatedUser(authentication);

        try {
            paymentService.markPaymentFailed(orderId.trim(), user.getId());
            return ResponseEntity.ok(Map.of("status", "marked_failed"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unable to mark payment failed"));
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new RuntimeException("Unauthorized");
        }

        return userRepository.findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}