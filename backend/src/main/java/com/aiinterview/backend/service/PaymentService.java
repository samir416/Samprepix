package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.PaymentRepository;
import com.aiinterview.backend.repository.PlanRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PlanService planService;

    @Value("${cashfree.client.id}")
    private String cashfreeClientId;

    @Value("${cashfree.client.secret}")
    private String cashfreeClientSecret;

    @Value("${cashfree.environment:TEST}")
    private String cashfreeEnvironment;

    private static final Set<String> VALID_REFERRAL_CODES = Set.of("SAMIR100");

    private String getCashfreeBaseUrl() {
        if ("PRODUCTION".equalsIgnoreCase(cashfreeEnvironment)) {
            return "https://api.cashfree.com/pg";
        }
        return "https://sandbox.cashfree.com/pg";
    }

    // =========================================================
    // CREATE CASHFREE ORDER
    // =========================================================

    @Transactional
    public Map<String, Object> createOrder(
            User user,
            PaymentRequest request,
            boolean isTestMode) {

        if (user == null || user.getId() == null) {
            throw new SecurityException("Authenticated user is required");
        }

        if (request == null || request.getPlanId() == null) {
            throw new IllegalArgumentException("Plan is required");
        }

        Plan plan;
        try {
            plan = planRepository.findById(Long.parseLong(request.getPlanId()))
                    .orElseThrow(() -> new RuntimeException("Plan not found"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid plan ID");
        }

        String planName = plan.getName();
        if (planName == null || planName.isBlank()) {
            throw new RuntimeException("Invalid plan configuration");
        }

        String currency = request.getCurrency() != null && !request.getCurrency().isBlank()
                ? request.getCurrency().trim().toUpperCase() : "INR";

        double amount;
        boolean referralApplied = false;
        double discountAmount = 0.0;

        if (isTestMode) {
            amount = planService.getTestPrice(planName);
        } else {
            boolean isIndia = "INR".equalsIgnoreCase(currency);
            amount = isIndia ? planService.getProductionPrice(planName) : planService.getProductionPriceUsd(planName);

            String referralCode = request.getReferralCode();
            if (referralCode != null && !referralCode.isBlank() && VALID_REFERRAL_CODES.contains(referralCode.trim().toUpperCase())) {
                if (!"STARTER".equalsIgnoreCase(planName)) {
                    discountAmount = isIndia ? 100.0 : 1.0;
                    amount = Math.max(0.0, amount - discountAmount);
                    referralApplied = true;
                }
            }
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        String orderId = "order_" + user.getId() + "_" + System.currentTimeMillis();
        String paymentSessionId = null;

        boolean isPlaceholder = cashfreeClientId == null || cashfreeClientId.isBlank() || cashfreeClientId.contains("placeholder") || cashfreeClientId.contains("test_client");

        if (isPlaceholder) {
            paymentSessionId = "session_mock_" + System.currentTimeMillis();
        } else {
            try {
                JSONObject customerDetails = new JSONObject();
                customerDetails.put("customer_id", "user_" + user.getId());
                customerDetails.put("customer_name", user.getName() != null ? user.getName() : "User");
                customerDetails.put("customer_email", user.getEmail());
                customerDetails.put("customer_phone", "9999999999");

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("order_amount", amount);
                orderRequest.put("order_currency", currency);
                orderRequest.put("order_id", orderId);
                orderRequest.put("customer_details", customerDetails);

                JSONObject orderMeta = new JSONObject();
                orderMeta.put("return_url", "http://localhost:5173/payment/verify?order_id={order_id}");
                orderRequest.put("order_meta", orderMeta);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(getCashfreeBaseUrl() + "/orders"))
                        .header("accept", "application/json")
                        .header("content-type", "application/json")
                        .header("x-client-id", cashfreeClientId)
                        .header("x-client-secret", cashfreeClientSecret)
                        .header("x-api-version", "2023-08-01")
                        .POST(HttpRequest.BodyPublishers.ofString(orderRequest.toString(), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    JSONObject jsonResponse = new JSONObject(resp.body());
                    paymentSessionId = jsonResponse.getString("payment_session_id");
                } else {
                    throw new RuntimeException("Failed to create Cashfree order. Status: " + resp.statusCode() + " Body: " + resp.body());
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Authentication failed")) {
                    paymentSessionId = "session_mock_" + System.currentTimeMillis();
                } else {
                    throw new RuntimeException("Failed to create Cashfree order: " + e.getMessage());
                }
            }
        }

        Optional<Payment> existingPayment = paymentRepository.findByCashfreeOrderId(orderId);
        if (existingPayment.isEmpty()) {
            Payment payment = Payment.builder()
                    .user(user)
                    .plan(plan)
                    .cashfreeOrderId(orderId)
                    .cashfreeSessionId(paymentSessionId)
                    .amount(amount)
                    .currency(currency)
                    .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "cashfree")
                    .paymentStatus("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId); // kept for frontend compatibility
        response.put("cashfreeOrderId", orderId);
        response.put("paymentSessionId", paymentSessionId);
        response.put("amount", amount);
        response.put("currency", currency);
        response.put("environment", cashfreeEnvironment);
        response.put("status", "CREATED");
        response.put("planId", plan.getId());
        response.put("planName", planName);
        response.put("testMode", isTestMode);
        response.put("referralApplied", referralApplied);
        if (referralApplied) {
            response.put("discountAmount", discountAmount);
        }
        return response;
    }

    // =========================================================
    // VERIFY PAYMENT SERVER-SIDE
    // =========================================================

    @Transactional
    public Map<String, Object> verifyAndActivatePayment(String orderId, Long authenticatedUserId) {
        if (authenticatedUserId == null) throw new SecurityException("Authenticated user is required");
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Missing order ID");

        // We check for cashfreeOrderId. If frontend sends razorpay_order_id, it will be handled appropriately by controller mapping.
        Payment payment = paymentRepository.findByCashfreeOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getUser() == null || payment.getUser().getId() == null || !payment.getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("Payment does not belong to authenticated user");
        }

        if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            Subscription existingSubscription = subscriptionRepository.findByCashfreeOrderId(orderId).orElse(null);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "already_success");
            response.put("message", "Payment already verified");
            if (existingSubscription != null) {
                response.put("subscriptionId", existingSubscription.getId());
            }
            return response;
        }

        // Verify with Cashfree API
        boolean isPlaceholder = cashfreeClientId == null || cashfreeClientId.isBlank() || cashfreeClientId.contains("placeholder") || cashfreeClientId.contains("test_client");
        boolean isPaid = false;

        if (isPlaceholder) {
            isPaid = true; // Mock success
        } else {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(getCashfreeBaseUrl() + "/orders/" + orderId))
                        .header("accept", "application/json")
                        .header("x-client-id", cashfreeClientId)
                        .header("x-client-secret", cashfreeClientSecret)
                        .header("x-api-version", "2023-08-01")
                        .GET()
                        .build();

                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    JSONObject jsonResponse = new JSONObject(resp.body());
                    String status = jsonResponse.optString("order_status");
                    if ("PAID".equalsIgnoreCase(status)) {
                        isPaid = true;
                    }
                } else {
                    throw new RuntimeException("Failed to verify Cashfree order. Status: " + resp.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Payment verification failed: " + e.getMessage());
            }
        }

        if (!isPaid) {
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);
            throw new RuntimeException("Payment is not in PAID status");
        }

        return activateEntitlements(payment, orderId);
    }

    private Map<String, Object> activateEntitlements(Payment payment, String orderId) {
        User user = payment.getUser();
        Plan plan = payment.getPlan();
        if (plan == null) throw new RuntimeException("Payment has no associated plan");

        payment.setPaymentStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Subscription subscription = subscriptionRepository.findByCashfreeOrderId(orderId).orElse(null);
        if (subscription == null) {
            List<Subscription> oldActiveSubs = subscriptionRepository.findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(user, "ACTIVE");
            for (Subscription oldSub : oldActiveSubs) {
                oldSub.setSubscriptionStatus("EXPIRED");
                oldSub.setAutoRenew(false);
                subscriptionRepository.save(oldSub);
            }

            subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .subscriptionStatus("ACTIVE")
                    .cashfreeOrderId(orderId)
                    .amountPaid(payment.getAmount())
                    .currency(payment.getCurrency())
                    .paymentMethod(payment.getPaymentMethod())
                    .autoRenew(false)
                    .build();
            subscriptionRepository.save(subscription);
            invoiceService.createInvoice(user, plan, payment, payment.getAmount(), payment.getCurrency());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment verified and subscription activated successfully");
        response.put("subscriptionId", subscription.getId());
        response.put("planName", plan.getName());
        return response;
    }

    // =========================================================
    // HANDLE WEBHOOK
    // =========================================================

    @Transactional
    public Map<String, Object> handleWebhook(String signature, String timestamp, String rawBody) {
        if (!verifyCashfreeSignature(timestamp, rawBody, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }

        try {
            JSONObject payload = new JSONObject(rawBody);
            JSONObject data = payload.optJSONObject("data");
            if (data == null) throw new IllegalArgumentException("Invalid payload structure");

            JSONObject order = data.optJSONObject("order");
            if (order == null) throw new IllegalArgumentException("Missing order in webhook");

            String orderId = order.optString("order_id");
            if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Missing order_id");

            Payment payment = paymentRepository.findByCashfreeOrderId(orderId).orElse(null);
            if (payment == null) {
                return Map.of("status", "ignored", "message", "Order not found in database");
            }

            if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
                return Map.of("status", "already_processed");
            }

            String paymentStatus = data.optJSONObject("payment").optString("payment_status");
            if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                return activateEntitlements(payment, orderId);
            } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
                payment.setPaymentStatus("FAILED");
                paymentRepository.save(payment);
                return Map.of("status", "marked_failed");
            }

            return Map.of("status", "ignored", "message", "Unhandled payment status");
        } catch (Exception e) {
            throw new RuntimeException("Webhook processing error: " + e.getMessage());
        }
    }

    private boolean verifyCashfreeSignature(String timestamp, String rawBody, String signature) {
        boolean isPlaceholder = cashfreeClientSecret == null || cashfreeClientSecret.isBlank() || cashfreeClientSecret.contains("placeholder");
        if (isPlaceholder) return true;

        try {
            String payload = timestamp + rawBody;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(cashfreeClientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = Base64.getEncoder().encodeToString(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // MARK PAYMENT FAILED
    // =========================================================

    @Transactional
    public void markPaymentFailed(String orderId, Long authenticatedUserId) {
        Payment payment = paymentRepository.findByCashfreeOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getUser() == null || payment.getUser().getId() == null || !payment.getUser().getId().equals(authenticatedUserId)) {
            throw new SecurityException("Payment does not belong to authenticated user");
        }

        if (!"SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);
        }
    }
}