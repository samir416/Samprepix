package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Refund;
import com.aiinterview.backend.entity.RefundStatus;
import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.PaymentRepository;
import com.aiinterview.backend.repository.PlanRepository;
import com.aiinterview.backend.repository.RefundRepository;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final InvoiceService invoiceService;
    private final PlanService planService;

    @Value("${cashfree.client.id}")
    private String cashfreeClientId;

    @Value("${cashfree.client.secret}")
    private String cashfreeClientSecret;

    @Value("${cashfree.environment:TEST}")
    private String cashfreeEnvironment;

    @Value("${cashfree.return.url:http://localhost:5173/payment/verify}")
    private String cashfreeReturnUrl;

    @Value("${cashfree.webhook.url:}")
    private String cashfreeWebhookUrl;

    private static final String CASHFREE_API_VERSION = "2025-01-01";

    private static final Set<String> VALID_REFERRAL_CODES =
            Set.of("SAMIR100");

    private HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }

    private String getCashfreeBaseUrl() {
        if ("PRODUCTION".equalsIgnoreCase(cashfreeEnvironment)) {
            return "https://api.cashfree.com/pg";
        }

        return "https://sandbox.cashfree.com/pg";
    }

    private HttpRequest.Builder cashfreeRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("x-client-id", cashfreeClientId)
                .header("x-client-secret", cashfreeClientSecret)
                .header("x-api-version", CASHFREE_API_VERSION);
    }

    @Transactional
    public Map<String, Object> createOrder(
            User user,
            PaymentRequest request,
            boolean isTestMode
    ) {
        if (user == null || user.getId() == null) {
            throw new SecurityException("Authenticated user is required");
        }

        if (request == null
                || request.getPlanId() == null
                || request.getPlanId().isBlank()) {
            throw new IllegalArgumentException("Plan is required");
        }

        Plan plan;

        try {
            plan = planRepository.findById(
                    Long.parseLong(request.getPlanId())
            ).orElseThrow(() ->
                    new IllegalArgumentException("Plan not found")
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid plan ID");
        }

        String planName = plan.getName();

        if (planName == null || planName.isBlank()) {
            throw new IllegalArgumentException("Invalid plan configuration");
        }

        String currency =
                request.getCurrency() != null
                        && !request.getCurrency().isBlank()
                        ? request.getCurrency().trim().toUpperCase()
                        : "INR";

        if (!"INR".equals(currency)
                && !"USD".equals(currency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }

        double displayAmount =
                "INR".equals(currency)
                        ? planService.getProductionPrice(planName)
                        : planService.getProductionPriceUsd(planName);

        double amount;
        boolean referralApplied = false;
        double discountAmount = 0.0;

        if (isTestMode) {
            amount = planService.getTestPrice(planName);
        } else {
            amount = displayAmount;

            boolean isIndia = "INR".equals(currency);

            String referralCode = request.getReferralCode();

            if (referralCode != null
                    && !referralCode.isBlank()
                    && VALID_REFERRAL_CODES.contains(
                    referralCode.trim().toUpperCase()
            )
                    && !"STARTER".equalsIgnoreCase(planName)) {

                discountAmount =
                        isIndia
                                ? 100.0
                                : 1.0;

                amount = Math.max(
                        0.0,
                        amount - discountAmount
                );

                displayAmount = amount;
                referralApplied = true;
            }
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        String orderId =
                "order_"
                        + user.getId()
                        + "_"
                        + System.currentTimeMillis();

        Optional<Payment> existingPayment =
                paymentRepository.findByCashfreeOrderId(orderId);

        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();

            return buildOrderResponse(
                    existing.getCashfreeOrderId(),
                    existing.getCashfreeSessionId(),
                    existing.getAmount(),
                    existing.getCurrency(),
                    existing.getDisplayAmount(),
                    plan,
                    isTestMode,
                    referralApplied,
                    discountAmount
            );
        }

        JSONObject customerDetails = new JSONObject();

        customerDetails.put(
                "customer_id",
                "user_" + user.getId()
        );

        customerDetails.put(
                "customer_name",
                user.getName() != null
                        && !user.getName().isBlank()
                        ? user.getName()
                        : "User"
        );

        customerDetails.put(
                "customer_email",
                user.getEmail()
        );

        customerDetails.put(
                "customer_phone",
                "9999999999"
        );

        JSONObject orderRequest = new JSONObject();

        orderRequest.put(
                "order_amount",
                amount
        );

        orderRequest.put(
                "order_currency",
                currency
        );

        orderRequest.put(
                "order_id",
                orderId
        );

        orderRequest.put(
                "customer_details",
                customerDetails
        );

        JSONObject orderMeta = new JSONObject();

        String returnUrl =
                cashfreeReturnUrl
                        + "?order_id={order_id}";

        orderMeta.put(
                "return_url",
                returnUrl
        );

        if (cashfreeWebhookUrl != null
                && !cashfreeWebhookUrl.isBlank()) {

            orderMeta.put(
                    "notify_url",
                    cashfreeWebhookUrl
            );
        }

        orderRequest.put(
                "order_meta",
                orderMeta
        );

        String paymentSessionId;

        try {
            HttpRequest httpRequest =
                    cashfreeRequestBuilder(
                            getCashfreeBaseUrl()
                                    + "/orders"
                    )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            orderRequest.toString(),
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    getHttpClient().send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Cashfree order creation failed. HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            JSONObject responseJson =
                    new JSONObject(response.body());

            paymentSessionId =
                    responseJson.optString(
                            "payment_session_id",
                            ""
                    );

            if (paymentSessionId.isBlank()) {
                throw new RuntimeException(
                        "Cashfree did not return payment_session_id"
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Cashfree order creation interrupted",
                    exception
            );

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to create Cashfree order: "
                            + exception.getMessage(),
                    exception
            );
        }

        Payment payment =
                Payment.builder()
                        .user(user)
                        .plan(plan)
                        .cashfreeOrderId(orderId)
                        .cashfreeSessionId(paymentSessionId)
                        .amount(amount)
                        .displayAmount(displayAmount)
                        .currency(currency)
                        .paymentMethod(
                                request.getPaymentMethod() != null
                                        && !request.getPaymentMethod().isBlank()
                                        ? request.getPaymentMethod()
                                        : "cashfree"
                        )
                        .paymentStatus("PENDING")
                        .testMode(isTestMode)
                        .createdAt(LocalDateTime.now())
                        .build();

        paymentRepository.save(payment);

        return buildOrderResponse(
                orderId,
                paymentSessionId,
                amount,
                currency,
                displayAmount,
                plan,
                isTestMode,
                referralApplied,
                discountAmount
        );
    }

    private Map<String, Object> buildOrderResponse(
            String orderId,
            String paymentSessionId,
            double amount,
            String currency,
            double displayAmount,
            Plan plan,
            boolean isTestMode,
            boolean referralApplied,
            double discountAmount
    ) {
        Map<String, Object> response =
                new HashMap<>();

        response.put("orderId", orderId);
        response.put("cashfreeOrderId", orderId);
        response.put("paymentSessionId", paymentSessionId);
        response.put("amount", amount);
        response.put("displayAmount", displayAmount);
        response.put("currency", currency);
        response.put("environment", cashfreeEnvironment);
        response.put("status", "CREATED");
        response.put("planId", plan.getId());
        response.put("planName", plan.getName());
        response.put("testMode", isTestMode);
        response.put("lifetime", false);
        response.put("subscriptionPeriod", "MONTH");
        response.put("autoRenew", true);
        response.put("referralApplied", referralApplied);

        if (referralApplied) {
            response.put("discountAmount", discountAmount);
        }

        return response;
    }

    @Transactional
    public Map<String, Object> verifyAndActivatePayment(
            String orderId,
            Long authenticatedUserId
    ) {
        if (authenticatedUserId == null) {
            throw new SecurityException(
                    "Authenticated user is required"
            );
        }

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing order ID"
            );
        }

        Payment payment =
                paymentRepository
                        .findByCashfreeOrderId(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        validatePaymentOwnership(
                payment,
                authenticatedUserId
        );

        if ("SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {
            return buildAlreadyProcessedResponse(
                    payment,
                    orderId
            );
        }

        CashfreeOrderStatus orderStatus =
                getCashfreeOrderStatus(orderId);

        if (orderStatus.paid()) {
            String paymentId =
                    orderStatus.paymentId();

            if (paymentId != null
                    && !paymentId.isBlank()) {

                payment.setCashfreePaymentId(
                        paymentId
                );

                paymentRepository.save(payment);
            }

            return activateEntitlements(
                    payment,
                    orderId
            );
        }

        if (orderStatus.failed()) {
            payment.setPaymentStatus("FAILED");
            paymentRepository.save(payment);

            throw new PaymentFailedException(
                    "Payment failed"
            );
        }

        return buildPendingResponse(
                payment,
                orderId
        );
    }

    private Map<String, Object> buildAlreadyProcessedResponse(
            Payment payment,
            String orderId
    ) {
        Subscription subscription =
                subscriptionRepository
                        .findByCashfreeOrderId(orderId)
                        .orElse(null);

        Map<String, Object> response =
                new HashMap<>();

        response.put("status", "already_success");
        response.put("paymentStatus", "SUCCESS");
        response.put("message", "Payment already verified");
        response.put("cashfreeOrderId", orderId);
        response.put("lifetime", false);
        response.put("subscriptionPeriod", "MONTH");
        response.put("premium", true);

        if (payment.getCashfreePaymentId() != null
                && !payment.getCashfreePaymentId().isBlank()) {

            response.put(
                    "cashfreePaymentId",
                    payment.getCashfreePaymentId()
            );
        }

        if (subscription != null) {
            response.put(
                    "subscriptionId",
                    subscription.getId()
            );

            response.put(
                    "planName",
                    subscription.getPlan() != null
                            ? subscription.getPlan().getName()
                            : "UNKNOWN"
            );

            response.put(
                    "subscriptionStatus",
                    subscription.getSubscriptionStatus()
            );

            response.put(
                    "expiresAt",
                    subscription.getExpiresAt()
            );

            response.put(
                    "autoRenew",
                    subscription.isAutoRenew()
            );
        }

        return response;
    }

    private Map<String, Object> buildPendingResponse(
            Payment payment,
            String orderId
    ) {
        Map<String, Object> response =
                new HashMap<>();

        response.put("status", "pending");
        response.put("subscriptionPeriod", "MONTH");
        response.put(
                "paymentStatus",
                payment.getPaymentStatus()
        );
        response.put(
                "message",
                "Payment is still being verified"
        );
        response.put(
                "cashfreeOrderId",
                orderId
        );
        response.put(
                "planName",
                payment.getPlan() != null
                        ? payment.getPlan().getName()
                        : "UNKNOWN"
        );

        return response;
    }

    private CashfreeOrderStatus getCashfreeOrderStatus(
            String orderId
    ) {
        try {
            HttpRequest request =
                    cashfreeRequestBuilder(
                            getCashfreeBaseUrl()
                                    + "/orders/"
                                    + orderId
                    )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    getHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Cashfree verification failed. HTTP "
                                + response.statusCode()
                );
            }

            JSONObject responseJson =
                    new JSONObject(
                            response.body()
                    );

            String orderStatus =
                    responseJson.optString(
                            "order_status",
                            ""
                    );

            if ("PAID".equalsIgnoreCase(orderStatus)) {
                return new CashfreeOrderStatus(
                        true,
                        false,
                        extractPaymentId(responseJson)
                );
            }

            if ("EXPIRED".equalsIgnoreCase(orderStatus)
                    || "TERMINATED".equalsIgnoreCase(orderStatus)) {

                return new CashfreeOrderStatus(
                        false,
                        true,
                        extractPaymentId(responseJson)
                );
            }

            return new CashfreeOrderStatus(
                    false,
                    false,
                    extractPaymentId(responseJson)
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Cashfree verification interrupted",
                    exception
            );

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Payment verification failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private String extractPaymentId(
            JSONObject orderResponse
    ) {
        String paymentId =
                orderResponse.optString(
                        "cf_payment_id",
                        ""
                );

        if (!paymentId.isBlank()) {
            return paymentId;
        }

        paymentId =
                orderResponse.optString(
                        "payment_id",
                        ""
                );

        return paymentId.isBlank()
                ? null
                : paymentId;
    }

    private void validatePaymentOwnership(
            Payment payment,
            Long authenticatedUserId
    ) {
        if (payment.getUser() == null
                || payment.getUser().getId() == null
                || !payment.getUser().getId().equals(
                authenticatedUserId
        )) {
            throw new SecurityException(
                    "Payment does not belong to authenticated user"
            );
        }
    }

    private Map<String, Object> activateEntitlements(
            Payment payment,
            String orderId
    ) {
        User user = payment.getUser();
        Plan plan = payment.getPlan();

        if (plan == null) {
            throw new RuntimeException(
                    "Payment has no associated plan"
            );
        }

        if ("SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {
            return buildAlreadyProcessedResponse(
                    payment,
                    orderId
            );
        }

        payment.setPaymentStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        Subscription subscription =
                subscriptionRepository
                        .findByCashfreeOrderId(orderId)
                        .orElse(null);

        if (subscription == null) {

            List<Subscription> oldActiveSubscriptions =
                    subscriptionRepository
                            .findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(
                                    user,
                                    "ACTIVE"
                            );

            for (Subscription oldSubscription :
                    oldActiveSubscriptions) {

                oldSubscription.setSubscriptionStatus(
                        "SUPERSEDED"
                );

                oldSubscription.setAutoRenew(false);

                oldSubscription.setExpiresAt(LocalDateTime.now());

                subscriptionRepository.save(
                        oldSubscription
                );
            }

            LocalDateTime subscribedAt = LocalDateTime.now();
            LocalDateTime expiresAt = subscribedAt.plusMonths(1);

            subscription =
                    Subscription.builder()
                            .user(user)
                            .plan(plan)
                            .subscriptionStatus("ACTIVE")
                            .cashfreeOrderId(orderId)
                            .cashfreeSessionId(
                                    payment.getCashfreeSessionId()
                            )
                            .cashfreePaymentId(
                                    payment.getCashfreePaymentId()
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
                            .lifetime(false)
                            .subscribedAt(subscribedAt)
                            .expiresAt(expiresAt)
                            .autoRenew(true)
                            .refundEligible(true)
                            .refundInitiated(false)
                            .build();

            subscriptionRepository.save(
                    subscription
            );

            invoiceService.createInvoice(
                    user,
                    plan,
                    payment,
                    payment.getAmount(),
                    payment.getCurrency()
            );
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("status", "success");
        response.put("paymentStatus", "SUCCESS");
        response.put(
                "message",
                "Payment verified and premium activated successfully"
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
                "cashfreeOrderId",
                orderId
        );
        response.put("lifetime", false);
        response.put("subscriptionPeriod", "MONTH");
        response.put("premium", true);
        response.put(
                "premiumBadge",
                plan.getName()
        );

        response.put(
                "expiresAt",
                subscription.getExpiresAt()
        );

        response.put(
                "autoRenew",
                subscription.isAutoRenew()
        );
        response.put(
                "subscriptionPeriod",
                "MONTH"
        );

        if (payment.getCashfreePaymentId() != null
                && !payment.getCashfreePaymentId().isBlank()) {

            response.put(
                    "cashfreePaymentId",
                    payment.getCashfreePaymentId()
            );
        }

        response.put(
                "amount",
                payment.getAmount()
        );

        response.put(
                "displayAmount",
                payment.getDisplayAmount()
        );

        response.put(
                "currency",
                payment.getCurrency()
        );

        return response;
    }

    @Transactional
    public Map<String, Object> handleWebhook(
            String signature,
            String timestamp,
            String rawBody
    ) {
        if (signature == null
                || signature.isBlank()
                || timestamp == null
                || timestamp.isBlank()
                || rawBody == null
                || rawBody.isBlank()) {

            throw new SecurityException(
                    "Missing webhook signature data"
            );
        }

        if (!verifyCashfreeSignature(
                timestamp,
                rawBody,
                signature
        )) {
            throw new SecurityException(
                    "Invalid webhook signature"
            );
        }

        try {
            JSONObject payload =
                    new JSONObject(rawBody);

            JSONObject data =
                    payload.optJSONObject("data");

            if (data == null) {
                throw new IllegalArgumentException(
                        "Invalid webhook payload"
                );
            }

            JSONObject order =
                    data.optJSONObject("order");

            if (order == null) {
                throw new IllegalArgumentException(
                        "Missing order in webhook"
                );
            }

            String orderId =
                    order.optString(
                            "order_id",
                            ""
                    );

            if (orderId.isBlank()) {
                throw new IllegalArgumentException(
                        "Missing order_id"
                );
            }

            Payment payment =
                    paymentRepository
                            .findByCashfreeOrderId(orderId)
                            .orElse(null);

            if (payment == null) {
                return Map.of(
                        "status",
                        "ignored",
                        "message",
                        "Order not found in database"
                );
            }

            if ("SUCCESS".equalsIgnoreCase(
                    payment.getPaymentStatus()
            )) {
                return Map.of(
                        "status",
                        "already_processed"
                );
            }

            JSONObject paymentData =
                    data.optJSONObject("payment");

            if (paymentData == null) {
                return Map.of(
                        "status",
                        "ignored",
                        "message",
                        "Payment data not present"
                );
            }

            String paymentStatus =
                    paymentData.optString(
                            "payment_status",
                            ""
                    );

            String paymentId =
                    paymentData.optString(
                            "cf_payment_id",
                            ""
                    );

            if (paymentId.isBlank()) {
                paymentId =
                        paymentData.optString(
                                "payment_id",
                                ""
                        );
            }

            if (!paymentId.isBlank()) {
                payment.setCashfreePaymentId(
                        paymentId
                );
            }

            if ("SUCCESS".equalsIgnoreCase(
                    paymentStatus
            )) {
                boolean verified =
                        verifyOrderWithCashfree(
                                orderId
                        );

                if (!verified) {
                    paymentRepository.save(payment);

                    return Map.of(
                            "status",
                            "pending",
                            "message",
                            "Payment received but final Cashfree verification is pending"
                    );
                }

                return activateEntitlements(
                        payment,
                        orderId
                );
            }

            if ("FAILED".equalsIgnoreCase(
                    paymentStatus
            )) {
                payment.setPaymentStatus("FAILED");
                paymentRepository.save(payment);

                return Map.of(
                        "status",
                        "marked_failed"
                );
            }

            paymentRepository.save(payment);

            return Map.of(
                    "status",
                    "pending",
                    "message",
                    "Payment is still being processed"
            );

        } catch (SecurityException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Webhook processing error: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private boolean verifyOrderWithCashfree(
            String orderId
    ) {
        try {
            HttpRequest request =
                    cashfreeRequestBuilder(
                            getCashfreeBaseUrl()
                                    + "/orders/"
                                    + orderId
                    )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    getHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Cashfree verification failed. HTTP "
                                + response.statusCode()
                );
            }

            JSONObject responseJson =
                    new JSONObject(
                            response.body()
                    );

            String orderStatus =
                    responseJson.optString(
                            "order_status",
                            ""
                    );

            return "PAID".equalsIgnoreCase(
                    orderStatus
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Cashfree verification interrupted",
                    exception
            );

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Payment verification failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private boolean verifyCashfreeSignature(
            String timestamp,
            String rawBody,
            String signature
    ) {
        if (cashfreeClientSecret == null
                || cashfreeClientSecret.isBlank()) {
            return false;
        }

        try {
            String payload =
                    timestamp + rawBody;

            Mac hmac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            cashfreeClientSecret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            hmac.init(secretKey);

            byte[] hash =
                    hmac.doFinal(
                            payload.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            String expectedSignature =
                    Base64.getEncoder()
                            .encodeToString(hash);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(
                            StandardCharsets.UTF_8
                    ),
                    signature.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

        } catch (Exception exception) {
            return false;
        }
    }

    @Transactional
    public void markPaymentFailed(
            String orderId,
            Long authenticatedUserId
    ) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing order ID"
            );
        }

        if (authenticatedUserId == null) {
            throw new SecurityException(
                    "Authenticated user is required"
            );
        }

        Payment payment =
                paymentRepository
                        .findByCashfreeOrderId(
                                orderId.trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"
                                )
                        );

        validatePaymentOwnership(
                payment,
                authenticatedUserId
        );

        if ("SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {
            return;
        }

        CashfreeOrderStatus orderStatus =
                getCashfreeOrderStatus(
                        orderId.trim()
                );

        if (orderStatus.paid()) {

            if (orderStatus.paymentId() != null
                    && !orderStatus.paymentId().isBlank()) {

                payment.setCashfreePaymentId(
                        orderStatus.paymentId()
                );
            }

            activateEntitlements(
                    payment,
                    orderId.trim()
            );

            return;
        }

        if (!orderStatus.failed()) {
            throw new PaymentFailedException(
                    "Payment is still pending. It cannot be marked as failed."
            );
        }

        payment.setPaymentStatus("FAILED");

        if (orderStatus.paymentId() != null
                && !orderStatus.paymentId().isBlank()) {

            payment.setCashfreePaymentId(
                    orderStatus.paymentId()
            );
        }

        paymentRepository.save(payment);
    }

    @Transactional
    public Map<String, Object> cancelAndRefundSubscription(
            Long subscriptionId,
            Long adminUserId,
            String reason
    ) {
        User admin = getAdminUser(adminUserId);

        if (subscriptionId == null) {
            throw new IllegalArgumentException(
                    "Subscription ID is required"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason is required"
            );
        }

        Subscription subscription =
                subscriptionRepository
                        .findById(subscriptionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscription not found"
                                )
                        );

        if (subscription.getUser() == null) {
            throw new RuntimeException(
                    "Subscription user not found"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                subscription.getSubscriptionStatus()
        )) {
            throw new IllegalStateException(
                    "Only active premium subscriptions can be cancelled"
            );
        }

        Payment payment =
                paymentRepository
                        .findByCashfreeOrderId(
                                subscription.getCashfreeOrderId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment record not found"
                                )
                        );

        if (!"SUCCESS".equalsIgnoreCase(
                payment.getPaymentStatus()
        )) {
            throw new IllegalStateException(
                    "Only successful payments can be refunded"
            );
        }

        if (!subscription.isRefundEligible()) {
            throw new IllegalStateException(
                    "This subscription is not eligible for refund"
            );
        }

        if (subscription.isRefundInitiated()) {
            throw new IllegalStateException(
                    "Refund has already been initiated"
            );
        }

        double refundAmount = payment.getAmount();

        if (refundAmount <= 0) {
            throw new IllegalStateException(
                    "Invalid refund amount"
            );
        }

        String refundId =
                "refund_"
                        + payment.getId()
                        + "_"
                        + System.currentTimeMillis();

        Refund refund =
                Refund.builder()
                        .user(subscription.getUser())
                        .payment(payment)
                        .subscription(subscription)
                        .refundId(refundId)
                        .cashfreeOrderId(
                                payment.getCashfreeOrderId()
                        )
                        .cashfreePaymentId(
                                payment.getCashfreePaymentId()
                        )
                        .refundAmount(refundAmount)
                        .currency(payment.getCurrency())
                        .refundStatus(
                                RefundStatus.PENDING.name()
                        )
                        .refundSpeed("STANDARD")
                        .initiatedBy(
                                admin.getEmail() != null
                                        ? admin.getEmail()
                                        : String.valueOf(admin.getId())
                        )
                        .reason(reason.trim())
                        .refundNote(
                                "Admin initiated premium cancellation refund"
                        )
                        .initiatedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build();

        refundRepository.save(refund);

        subscription.setSubscriptionStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);
        subscription.setExpiresAt(LocalDateTime.now());
        subscription.setCancelledBy(
                admin.getEmail() != null
                        ? admin.getEmail()
                        : String.valueOf(admin.getId())
        );
        subscription.setCancellationReason(
                reason.trim()
        );
        subscription.setRefundInitiated(true);
        subscription.setRefundId(refundId);
        subscription.setRefundStatus(
                RefundStatus.PENDING.name()
        );
        subscription.setRefundInitiatedAt(
                LocalDateTime.now()
        );
        subscription.setRefundReason(
                reason.trim()
        );

        payment.setRefundId(refundId);
        payment.setRefundStatus(
                RefundStatus.PENDING.name()
        );
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(
                reason.trim()
        );
        payment.setRefundInitiatedAt(
                LocalDateTime.now()
        );
        payment.setCancelledBy(
                admin.getEmail() != null
                        ? admin.getEmail()
                        : String.valueOf(admin.getId())
        );
        payment.setCancellationReason(
                reason.trim()
        );
        payment.setPaymentStatus("REFUND_PENDING");

        subscriptionRepository.save(subscription);
        paymentRepository.save(payment);

        try {
            JSONObject refundRequest =
                    new JSONObject();

            refundRequest.put(
                    "refund_amount",
                    refundAmount
            );

            refundRequest.put(
                    "refund_id",
                    refundId
            );

            refundRequest.put(
                    "refund_note",
                    reason.trim()
            );

            refundRequest.put(
                    "refund_speed",
                    "STANDARD"
            );

            HttpRequest request =
                    cashfreeRequestBuilder(
                            getCashfreeBaseUrl()
                                    + "/orders/"
                                    + payment.getCashfreeOrderId()
                                    + "/refunds"
                    )
                            .header(
                                    "x-idempotency-key",
                                    UUID.randomUUID().toString()
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            refundRequest.toString(),
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    getHttpClient().send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                refund.setRefundStatus(
                        RefundStatus.FAILED.name()
                );

                refund.setFailureReason(
                        response.body()
                );

                refund.setFailedAt(
                        LocalDateTime.now()
                );

                refundRepository.save(refund);

                subscription.setRefundStatus(
                        RefundStatus.FAILED.name()
                );

                subscriptionRepository.save(
                        subscription
                );

                payment.setRefundStatus(
                        RefundStatus.FAILED.name()
                );

                paymentRepository.save(payment);

                throw new RuntimeException(
                        "Cashfree refund initiation failed. HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            JSONObject responseJson =
                    new JSONObject(
                            response.body()
                    );

            String cashfreeRefundId =
                    responseJson.optString(
                            "cf_refund_id",
                            ""
                    );

            if (cashfreeRefundId.isBlank()) {
                cashfreeRefundId =
                        responseJson.optString(
                                "refund_id",
                                refundId
                        );
            }

            String refundStatus =
                    responseJson.optString(
                            "refund_status",
                            "PENDING"
                    );

            refund.setRefundId(
                    cashfreeRefundId
            );

            refund.setRefundStatus(
                    normalizeRefundStatus(
                            refundStatus
                    )
            );

            refund.setProcessedAt(
                    LocalDateTime.now()
            );

            if ("SUCCESS".equalsIgnoreCase(
                    refund.getRefundStatus()
            )) {

                refund.setCompletedAt(
                        LocalDateTime.now()
                );

                subscription.setRefundStatus(
                        RefundStatus.SUCCESS.name()
                );

                subscription.setRefundedAt(
                        LocalDateTime.now()
                );

                payment.setRefundStatus(
                        RefundStatus.SUCCESS.name()
                );

                payment.setRefundedAt(
                        LocalDateTime.now()
                );

                payment.setPaymentStatus(
                        "REFUNDED"
                );

            } else {
                subscription.setRefundStatus(
                        RefundStatus.PENDING.name()
                );

                payment.setRefundStatus(
                        RefundStatus.PENDING.name()
                );
            }

            subscription.setRefundId(
                    cashfreeRefundId
            );

            payment.setRefundId(
                    cashfreeRefundId
            );

            refundRepository.save(refund);
            subscriptionRepository.save(subscription);
            paymentRepository.save(payment);

            return buildRefundResponse(
                    refund,
                    subscription,
                    payment
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            refund.setRefundStatus(
                    RefundStatus.FAILED.name()
            );

            refund.setFailureReason(
                    "Cashfree refund request interrupted"
            );

            refund.setFailedAt(
                    LocalDateTime.now()
            );

            refundRepository.save(refund);

            throw new RuntimeException(
                    "Cashfree refund request interrupted",
                    exception
            );

        } catch (RuntimeException exception) {
            throw exception;

        } catch (Exception exception) {
            refund.setRefundStatus(
                    RefundStatus.FAILED.name()
            );

            refund.setFailureReason(
                    exception.getMessage()
            );

            refund.setFailedAt(
                    LocalDateTime.now()
            );

            refundRepository.save(refund);

            throw new RuntimeException(
                    "Refund processing failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRefundDetails(
            Long refundDbId,
            Long adminUserId
    ) {
        getAdminUser(adminUserId);

        if (refundDbId == null) {
            throw new IllegalArgumentException(
                    "Refund ID is required"
            );
        }

        Refund refund =
                refundRepository
                        .findById(refundDbId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Refund not found"
                                )
                        );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                refund.getId()
        );

        response.put(
                "refundId",
                refund.getRefundId()
        );

        response.put(
                "cashfreeOrderId",
                refund.getCashfreeOrderId()
        );

        response.put(
                "cashfreePaymentId",
                refund.getCashfreePaymentId()
        );

        response.put(
                "refundAmount",
                refund.getRefundAmount()
        );

        response.put(
                "currency",
                refund.getCurrency()
        );

        response.put(
                "refundStatus",
                refund.getRefundStatus()
        );

        response.put(
                "refundSpeed",
                refund.getRefundSpeed()
        );

        response.put(
                "reason",
                refund.getReason()
        );

        response.put(
                "refundNote",
                refund.getRefundNote()
        );

        response.put(
                "failureReason",
                refund.getFailureReason()
        );

        response.put(
                "initiatedBy",
                refund.getInitiatedBy()
        );

        response.put(
                "initiatedAt",
                refund.getInitiatedAt()
        );

        response.put(
                "processedAt",
                refund.getProcessedAt()
        );

        response.put(
                "completedAt",
                refund.getCompletedAt()
        );

        response.put(
                "failedAt",
                refund.getFailedAt()
        );

        return response;
    }

    private Map<String, Object> buildRefundResponse(
            Refund refund,
            Subscription subscription,
            Payment payment
    ) {
        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "status",
                refund.getRefundStatus()
        );

        response.put(
                "refundId",
                refund.getRefundId()
        );

        response.put(
                "refundAmount",
                refund.getRefundAmount()
        );

        response.put(
                "currency",
                refund.getCurrency()
        );

        response.put(
                "subscriptionId",
                subscription.getId()
        );

        response.put(
                "paymentId",
                payment.getId()
        );

        response.put(
                "premiumRevoked",
                true
        );

        response.put(
                "subscriptionStatus",
                subscription.getSubscriptionStatus()
        );

        response.put(
                "refundStatus",
                refund.getRefundStatus()
        );

        return response;
    }

    private String normalizeRefundStatus(
            String status
    ) {
        if (status == null || status.isBlank()) {
            return RefundStatus.PENDING.name();
        }

        String normalized =
                status.trim().toUpperCase();

        if ("SUCCESS".equals(normalized)
                || "COMPLETED".equals(normalized)
                || "REFUNDED".equals(normalized)) {

            return RefundStatus.SUCCESS.name();
        }

        if ("FAILED".equals(normalized)
                || "FAILURE".equals(normalized)) {

            return RefundStatus.FAILED.name();
        }

        if ("CANCELLED".equals(normalized)
                || "CANCELED".equals(normalized)) {

            return RefundStatus.CANCELLED.name();
        }

        return RefundStatus.PENDING.name();
    }

    private User getAdminUser(
            Long adminUserId
    ) {
        if (adminUserId == null) {
            throw new SecurityException(
                    "Admin authentication is required"
            );
        }

        User admin =
                userRepository
                        .findById(adminUserId)
                        .orElseThrow(() ->
                                new SecurityException(
                                        "Admin user not found"
                                )
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new SecurityException(
                    "Admin access required"
            );
        }

        return admin;
    }

    private record CashfreeOrderStatus(
            boolean paid,
            boolean failed,
            String paymentId
    ) {
    }

    public static class PaymentFailedException
            extends RuntimeException {

        public PaymentFailedException(
                String message
        ) {
            super(message);
        }
    }
}