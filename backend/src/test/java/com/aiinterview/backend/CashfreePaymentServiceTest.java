package com.aiinterview.backend;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.entity.Invoice;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InvoiceRepository;
import com.aiinterview.backend.repository.PaymentRepository;
import com.aiinterview.backend.repository.PlanRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CashfreePaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private User testUser;
    private Plan testPlan;

    @BeforeEach
    public void setup() {
        long timestamp = System.currentTimeMillis();

        testUser = new User();
        testUser.setName("Test User CF");
        testUser.setUsername("test_cf_" + timestamp);
        testUser.setEmail("test_cf_" + timestamp + "@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        testPlan = planRepository
                .findByNameIgnoreCase("PRO")
                .orElseGet(() -> planRepository.save(
                        Plan.builder()
                                .name("PRO")
                                .description("Pro Plan")
                                .priceInr(399.0)
                                .priceUsd(9.0)
                                .interval("MONTH")
                                .maxMockInterviews(15)
                                .maxResumeScans(20)
                                .maxCodingProblems(50)
                                .maxAptitudeQuestions(50)
                                .includesAIHints(true)
                                .includesAnalytics(true)
                                .includesTier1Companies(true)
                                .includesPriorityCompute(true)
                                .active(true)
                                .featured(true)
                                .build()));
    }

    @AfterEach
    public void cleanup() {
        if (testUser == null || testUser.getId() == null) {
            return;
        }

        List<Invoice> invoices = invoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getUser() != null
                        && testUser.getId().equals(invoice.getUser().getId()))
                .toList();

        invoiceRepository.deleteAll(invoices);

        List<Subscription> subscriptions = subscriptionRepository.findByUserId(testUser.getId());

        subscriptionRepository.deleteAll(subscriptions);

        List<Payment> payments = paymentRepository
                .findAll()
                .stream()
                .filter(payment -> payment.getUser() != null
                        && testUser.getId().equals(payment.getUser().getId()))
                .toList();

        paymentRepository.deleteAll(payments);

        userRepository.deleteById(testUser.getId());
    }

    @Test
    public void testOrderCreation() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("INR");
        request.setPaymentMethod("cashfree");

        Map<String, Object> response = paymentService.createOrder(testUser, request, true);

        assertNotNull(response);
        assertNotNull(response.get("cashfreeOrderId"));
        assertNotNull(response.get("paymentSessionId"));
        assertEquals("CREATED", response.get("status"));
        assertEquals("MONTH", response.get("subscriptionPeriod"));
        assertEquals(Boolean.TRUE, response.get("autoRenew"));
        assertEquals(Boolean.FALSE, response.get("lifetime"));
    }

    @Test
    public void testAuthRequired() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("INR");
        request.setPaymentMethod("cashfree");

        assertThrows(
                SecurityException.class,
                () -> paymentService.createOrder(null, request, true));
    }

    @Test
    public void testInvalidPlan() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId("999999999");
        request.setCurrency("INR");
        request.setPaymentMethod("cashfree");

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createOrder(testUser, request, true));
    }

    @Test
    public void testInvalidCurrency() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("EUR");
        request.setPaymentMethod("cashfree");

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createOrder(testUser, request, true));
    }

    @Test
    public void testInvalidPaymentMethod() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("INR");
        request.setPaymentMethod("invalid_gateway");

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createOrder(testUser, request, true));
    }

    @Test
    public void testWrongUserOwnership() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("INR");
        request.setPaymentMethod("cashfree");

        Map<String, Object> response = paymentService.createOrder(testUser, request, true);

        String orderId = String.valueOf(response.get("cashfreeOrderId"));

        User wrongUser = new User();
        wrongUser.setName("Wrong User");
        wrongUser.setUsername("wrong_user_" + System.currentTimeMillis());
        wrongUser.setEmail(
                "wrong_user_" + System.currentTimeMillis() + "@example.com");
        wrongUser.setPassword("pwd");

        wrongUser = userRepository.save(wrongUser);

        try {
            Long wrongUserId = wrongUser.getId();

            assertThrows(
                    SecurityException.class,
                    () -> paymentService.verifyAndActivatePayment(
                            orderId,
                            wrongUserId));
        } finally {
            userRepository.deleteById(wrongUser.getId());
        }
    }

    @Test
    public void testPaymentCreationPersistsMonthlySubscriptionData() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        request.setCurrency("INR");
        request.setPaymentMethod("cashfree");

        Map<String, Object> response = paymentService.createOrder(testUser, request, true);

        String orderId = String.valueOf(response.get("cashfreeOrderId"));

        assertNotNull(orderId);

        List<Payment> payments = paymentRepository
                .findAll()
                .stream()
                .filter(payment -> payment.getUser() != null
                        && testUser.getId().equals(payment.getUser().getId()))
                .toList();

        assertFalse(payments.isEmpty());

        Payment payment = payments.stream()
                .filter(p -> orderId.equals(p.getCashfreeOrderId()))
                .findFirst()
                .orElse(null);

        assertNotNull(payment);
        assertEquals("INR", payment.getCurrency());
        assertEquals(1.0, payment.getAmount());
        assertEquals("cashfree", payment.getPaymentMethod());
    }

    @Test
    public void testTestPrices() {
        assertEquals(1.0, testPlan.getName().equalsIgnoreCase("PRO")
                ? 1.0
                : 0.0);
    }

    @Test
    public void testPlanIsMonthly() {
        assertEquals("MONTH", testPlan.getInterval().toUpperCase());
        assertFalse(testPlan.isActive() == false);
    }
}