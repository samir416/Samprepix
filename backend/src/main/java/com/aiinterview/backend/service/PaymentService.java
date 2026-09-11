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
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.aiinterview.backend.entity.Invoice;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PlanService planService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    /*
     * Backend-controlled referral codes.
     * Frontend cannot change the final amount.
     */
    private static final Set<String> VALID_REFERRAL_CODES =
            Set.of("SAMIR100");

    // =========================================================
    // CREATE RAZORPAY ORDER
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
            plan = planRepository.findById(
                    Long.parseLong(request.getPlanId())
            ).orElseThrow(() ->
                    new RuntimeException("Plan not found")
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid plan ID");
        }

        String planName = plan.getName();

        if (planName == null || planName.isBlank()) {
            throw new RuntimeException("Invalid plan configuration");
        }

        String currency =
                request.getCurrency() != null
                        && !request.getCurrency().isBlank()
                        ? request.getCurrency().trim().toUpperCase()
                        : "INR";

        double amount;
        boolean referralApplied = false;
        double discountAmount = 0.0;

        // ---------------------------------------------------------
        // SERVER-SIDE PRICING
        // ---------------------------------------------------------

        if (isTestMode) {

            amount = planService.getTestPrice(planName);

        } else {

            boolean isIndia =
                    "INR".equalsIgnoreCase(currency);

            amount = isIndia
                    ? planService.getProductionPrice(planName)
                    : planService.getProductionPriceUsd(planName);

            // -----------------------------------------------------
            // SERVER-SIDE REFERRAL VALIDATION
            // -----------------------------------------------------

            String referralCode =
                    request.getReferralCode();

            if (referralCode != null
                    && !referralCode.isBlank()
                    && VALID_REFERRAL_CODES.contains(
                    referralCode.trim().toUpperCase()
            )) {

                if (!"STARTER".equalsIgnoreCase(planName)) {

                    discountAmount =
                            isIndia
                                    ? 100.0
                                    : 1.0;

                    amount =
                            Math.max(
                                    0.0,
                                    amount - discountAmount
                            );

                    referralApplied = true;
                }
            }
        }

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Invalid payment amount"
            );
        }

        int amountInPaise =
                (int) Math.round(amount * 100);

        if (amountInPaise < 0) {
            throw new IllegalArgumentException(
                    "Invalid payment amount"
            );
        }

        String orderId;
        boolean isPlaceholder = razorpayKeyId == null || razorpayKeyId.isBlank()
                || razorpayKeyId.contains("placeholder") || razorpayKeyId.contains("mock");

        if (isPlaceholder) {
            orderId = "order_test_" + System.currentTimeMillis() + "_" + Math.round(amount);
        } else {
            try {
                RazorpayClient razorpay =
                        new RazorpayClient(
                                razorpayKeyId,
                                razorpayKeySecret
                        );

                JSONObject orderRequest =
                        new JSONObject();

                orderRequest.put(
                        "amount",
                        amountInPaise
                );

                orderRequest.put(
                        "currency",
                        currency
                );

                orderRequest.put(
                        "receipt",
                        "receipt_"
                                + user.getId()
                                + "_"
                                + System.currentTimeMillis()
                );

                com.razorpay.Order razorpayOrder =
                        razorpay.orders.create(
                                orderRequest
                        );

                orderId = razorpayOrder.get("id");

                if (orderId == null || orderId.isBlank()) {
                    throw new RuntimeException(
                            "Razorpay did not return an order ID"
                    );
                }
            } catch (RazorpayException e) {
                if (e.getMessage() != null && e.getMessage().contains("Authentication failed")) {
                    orderId = "order_test_" + System.currentTimeMillis() + "_" + Math.round(amount);
                } else {
                    throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
                }
            }
        }

            Optional<Payment> existingPayment =
                    paymentRepository
                            .findByRazorpayOrderId(orderId);

            if (existingPayment.isEmpty()) {

                Payment payment =
                        Payment.builder()
                                .user(user)
                                .plan(plan)
                                .razorpayOrderId(orderId)
                                .razorpayPaymentId("pending")
                                .razorpaySignature("pending")
                                .amount(amount)
                                .currency(currency)
                                .paymentMethod(
                                        request.getPaymentMethod() != null
                                                ? request.getPaymentMethod()
                                                : "razorpay"
                                )
                                .paymentStatus("PENDING")
                                .paidAt(null)
                                .createdAt(
                                        LocalDateTime.now()
                                )
                                .build();

                paymentRepository.save(payment);
            }

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "orderId",
                    orderId
            );

            response.put(
                    "razorpayOrderId",
                    orderId
            );

            response.put(
                    "amount",
                    amount
            );

            response.put(
                    "currency",
                    currency
            );

            response.put(
                    "key",
                    razorpayKeyId
            );

            response.put(
                    "status",
                    "CREATED"
            );

            response.put(
                    "planId",
                    plan.getId()
            );

            response.put(
                    "planName",
                    planName
            );

            response.put(
                    "testMode",
                    isTestMode
            );

            response.put(
                    "referralApplied",
                    referralApplied
            );

            if (referralApplied) {
                response.put(
                        "discountAmount",
                        discountAmount
                );
            }

            return response;
    }

    // =========================================================
    // VERIFY PAYMENT
    // =========================================================

    @Transactional
    public Map<String, Object> verifyAndActivatePayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            Long authenticatedUserId) {

        if (authenticatedUserId == null) {
            throw new SecurityException(
                    "Authenticated user is required"
            );
        }

        if (isBlank(razorpayOrderId)
                || isBlank(razorpayPaymentId)
                || isBlank(razorpaySignature)) {

            throw new IllegalArgumentException(
                    "Missing payment verification parameters"
            );
        }

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                razorpayOrderId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        // ---------------------------------------------------------
        // IDOR PROTECTION
        // ---------------------------------------------------------

        if (payment.getUser() == null
                || payment.getUser().getId() == null
                || !payment.getUser()
                .getId()
                .equals(authenticatedUserId)) {

            throw new SecurityException(
                    "Payment does not belong to authenticated user"
            );
        }

        // ---------------------------------------------------------
        // IDEMPOTENCY
        // ---------------------------------------------------------

        if ("SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {

            Subscription existingSubscription =
                    subscriptionRepository
                            .findByRazorpayOrderId(
                                    razorpayOrderId
                            )
                            .orElse(null);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "status",
                    "already_success"
            );

            response.put(
                    "message",
                    "Payment already verified"
            );

            if (existingSubscription != null) {
                response.put(
                        "subscriptionId",
                        existingSubscription.getId()
                );
            }

            return response;
        }

        // ---------------------------------------------------------
        // RAZORPAY SIGNATURE
        // ---------------------------------------------------------

        if (!verifyRazorpaySignature(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature
        )) {

            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);

            throw new RuntimeException(
                    "Invalid payment signature"
            );
        }

        User user =
                payment.getUser();

        Plan plan =
                payment.getPlan();

        if (plan == null) {
            throw new RuntimeException(
                    "Payment has no associated plan"
            );
        }

        double amount =
                payment.getAmount();

        String currency =
                payment.getCurrency();

        // ---------------------------------------------------------
        // MARK PAYMENT SUCCESS
        // ---------------------------------------------------------

        payment.setRazorpayPaymentId(
                razorpayPaymentId
        );

        payment.setRazorpaySignature(
                razorpaySignature
        );

        payment.setPaymentStatus(
                "SUCCESS"
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        paymentRepository.save(payment);

        // ---------------------------------------------------------
        // CREATE SUBSCRIPTION ONCE
        // ---------------------------------------------------------

        Subscription subscription =
                subscriptionRepository
                        .findByRazorpayOrderId(
                                razorpayOrderId
                        )
                        .orElse(null);

        if (subscription == null) {

            LocalDateTime now =
                    LocalDateTime.now();

            subscription =
                    Subscription.builder()
                            .user(user)
                            .plan(plan)
                            .subscriptionStatus("ACTIVE")
                            .razorpaySubscriptionId(
                                    null
                            )
                            .razorpayOrderId(
                                    razorpayOrderId
                            )
                            .razorpayPaymentId(
                                    razorpayPaymentId
                            )
                            .amountPaid(amount)
                            .currency(currency)
                            .paymentMethod(
                                    payment.getPaymentMethod()
                            )
                            .subscribedAt(now)
                            .expiresAt(
                                    now.plusMonths(1)
                            )
                            .autoRenew(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

            subscriptionRepository.save(
                    subscription
            );
        }

        // ---------------------------------------------------------
        // INVOICE
        // ---------------------------------------------------------

        Invoice invoice =
                invoiceService.createInvoice(
                        user,
                        plan,
                        payment,
                        amount,
                        currency
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "status",
                "success"
        );

        response.put(
                "message",
                "Payment verified and subscription activated"
        );

        response.put(
                "invoiceNumber",
                invoice.getInvoiceNumber()
        );

        response.put(
                "subscriptionId",
                subscription.getId()
        );

        response.put(
                "planName",
                plan.getName()
        );

        response.put(
                "expiresAt",
                subscription.getExpiresAt()
        );

        return response;
    }

    // =========================================================
    // RAZORPAY WEBHOOK
    // =========================================================

    @Transactional
    public Map<String, Object> handleWebhook(
            String razorpaySignature,
            String rawBody) {

        if (isBlank(razorpaySignature)) {
            throw new SecurityException(
                    "Missing Razorpay webhook signature"
            );
        }

        if (isBlank(rawBody)) {
            throw new IllegalArgumentException(
                    "Empty webhook payload"
            );
        }

        // ---------------------------------------------------------
        // WEBHOOK SIGNATURE MUST BE VERIFIED AGAINST RAW BODY
        // ---------------------------------------------------------

        if (!verifyWebhookSignature(
                rawBody,
                razorpaySignature
        )) {

            throw new SecurityException(
                    "Invalid Razorpay webhook signature"
            );
        }

        try {

            JSONObject root =
                    new JSONObject(rawBody);

            String event =
                    root.optString(
                            "event",
                            ""
                    );

            if (!"payment.captured".equals(event)
                    && !"order.paid".equals(event)) {

                return Map.of(
                        "status",
                        "ok",
                        "message",
                        "Event not processed"
                );
            }

            JSONObject paymentEntity =
                    root.optJSONObject("payload")
                            .optJSONObject("payment")
                            .optJSONObject("entity");

            if (paymentEntity == null) {
                throw new IllegalArgumentException(
                        "Invalid Razorpay webhook payload"
                );
            }

            String orderId =
                    paymentEntity.optString(
                            "order_id",
                            ""
                    );

            String paymentId =
                    paymentEntity.optString(
                            "id",
                            ""
                    );

            if (isBlank(orderId)
                    || isBlank(paymentId)) {

                throw new IllegalArgumentException(
                        "Webhook payment information is missing"
                );
            }

            Payment payment =
                    paymentRepository
                            .findByRazorpayOrderId(
                                    orderId
                            )
                            .orElse(null);

            /*
             * Never create a payment/subscription for an unknown
             * order. Every legitimate order must first be created
             * by our authenticated createOrder endpoint.
             */
            if (payment == null) {

                return Map.of(
                        "status",
                        "ok",
                        "message",
                        "Unknown order ignored"
                );
            }

            // -----------------------------------------------------
            // IDEMPOTENCY
            // -----------------------------------------------------

            if ("SUCCESS".equalsIgnoreCase(
                    payment.getPaymentStatus()
            )) {

                return Map.of(
                        "status",
                        "ok",
                        "message",
                        "Already processed - idempotent"
                );
            }

            // -----------------------------------------------------
            // VALIDATE PAYMENT AMOUNT
            // -----------------------------------------------------

            double webhookAmount =
                    paymentEntity.optDouble(
                            "amount",
                            -1
                    ) / 100.0;

            if (webhookAmount < 0) {

                throw new IllegalArgumentException(
                        "Invalid webhook amount"
                );
            }

            if (Math.abs(
                    webhookAmount - payment.getAmount()
            ) > 0.01) {

                throw new SecurityException(
                        "Webhook amount does not match order amount"
                );
            }

            // -----------------------------------------------------
            // UPDATE PAYMENT
            // -----------------------------------------------------

            payment.setRazorpayPaymentId(
                    paymentId
            );

            /*
             * Webhook verification uses the webhook signature,
             * not the checkout payment signature.
             */
            payment.setRazorpaySignature(
                    "WEBHOOK_VERIFIED"
            );

            payment.setPaymentStatus(
                    "SUCCESS"
            );

            payment.setPaidAt(
                    LocalDateTime.now()
            );

            paymentRepository.save(payment);

            User user =
                    payment.getUser();

            Plan plan =
                    payment.getPlan();

            if (user == null || plan == null) {

                throw new IllegalStateException(
                        "Payment is missing user or plan"
                );
            }

            // -----------------------------------------------------
            // CREATE SUBSCRIPTION ONCE
            // -----------------------------------------------------

            Subscription subscription =
                    subscriptionRepository
                            .findByRazorpayOrderId(
                                    orderId
                            )
                            .orElse(null);

            if (subscription == null) {

                LocalDateTime now =
                        LocalDateTime.now();

                subscription =
                        Subscription.builder()
                                .user(user)
                                .plan(plan)
                                .subscriptionStatus("ACTIVE")
                                .razorpaySubscriptionId(
                                        null
                                )
                                .razorpayOrderId(
                                        orderId
                                )
                                .razorpayPaymentId(
                                        paymentId
                                )
                                .amountPaid(
                                        payment.getAmount()
                                )
                                .currency(
                                        payment.getCurrency()
                                )
                                .paymentMethod(
                                        payment.getPaymentMethod()
                                )
                                .subscribedAt(now)
                                .expiresAt(
                                        now.plusMonths(1)
                                )
                                .autoRenew(true)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                subscriptionRepository.save(
                        subscription
                );
            }

            // -----------------------------------------------------
            // CREATE INVOICE
            // -----------------------------------------------------

            invoiceService.createInvoice(
                    user,
                    plan,
                    payment,
                    payment.getAmount(),
                    payment.getCurrency()
            );

            return Map.of(
                    "status",
                    "ok",
                    "message",
                    "Webhook processed"
            );

        } catch (SecurityException e) {

            throw e;

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Webhook processing failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // CHECKOUT PAYMENT SIGNATURE
    // =========================================================

    private boolean verifyRazorpaySignature(
            String orderId,
            String paymentId,
            String signature) {

        if (isBlank(orderId)
                || isBlank(paymentId)
                || isBlank(signature)
                || isBlank(razorpayKeySecret)) {

            return false;
        }

        String data =
                orderId + "|" + paymentId;

        return verifyHmacSha256(
                data,
                signature,
                razorpayKeySecret
        );
    }

    // =========================================================
    // WEBHOOK SIGNATURE
    // =========================================================

    private boolean verifyWebhookSignature(
            String rawBody,
            String signature) {

        if (isBlank(rawBody)
                || isBlank(signature)
                || isBlank(webhookSecret)) {

            return false;
        }

        return verifyHmacSha256(
                rawBody,
                signature,
                webhookSecret
        );
    }

    // =========================================================
    // HMAC-SHA256
    // =========================================================

    private boolean verifyHmacSha256(
            String data,
            String providedSignature,
            String secret) {

        try {

            Mac mac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] hash =
                    mac.doFinal(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder(
                            hash.length * 2
                    );

            for (byte b : hash) {

                hex.append(
                        String.format(
                                "%02x",
                                b & 0xff
                        )
                );
            }

            byte[] expected =
                    hex.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );

            byte[] actual =
                    providedSignature
                            .trim()
                            .toLowerCase()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );

            return MessageDigest.isEqual(
                    expected,
                    actual
            );

        } catch (Exception e) {

            return false;
        }
    }

    // =========================================================
    // MARK PAYMENT FAILED
    // =========================================================

    @Transactional
    public void markPaymentFailed(
            String razorpayOrderId,
            Long authenticatedUserId) {

        if (authenticatedUserId == null) {

            throw new SecurityException(
                    "Authenticated user is required"
            );
        }

        if (isBlank(razorpayOrderId)) {

            throw new IllegalArgumentException(
                    "Missing razorpay order ID"
            );
        }

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                razorpayOrderId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        // ---------------------------------------------------------
        // IDOR PROTECTION
        // ---------------------------------------------------------

        if (payment.getUser() == null
                || payment.getUser().getId() == null
                || !payment.getUser()
                .getId()
                .equals(authenticatedUserId)) {

            throw new SecurityException(
                    "Payment does not belong to authenticated user"
            );
        }

        // Never change an already successful payment.
        if ("SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {
            return;
        }

        payment.setPaymentStatus(
                "FAILED"
        );

        paymentRepository.save(payment);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}