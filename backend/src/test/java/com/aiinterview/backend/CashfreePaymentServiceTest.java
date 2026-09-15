package com.aiinterview.backend;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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

    private User testUser;
    private Plan testPlan;

    @BeforeEach
    public void setup() {
        User u = new User();
        u.setName("Test User CF");
        u.setUsername("test_cf_" + System.currentTimeMillis());
        u.setEmail("test_cf_" + System.currentTimeMillis() + "@example.com");
        u.setPassword("password");
        testUser = userRepository.save(u);

        testPlan = planRepository.findAll().stream().filter(p -> "PRO".equalsIgnoreCase(p.getName())).findFirst().orElse(null);
        if (testPlan == null) {
            testPlan = planRepository.save(Plan.builder()
                    .name("PRO")
                    .description("Pro Plan")
                    .build());
        }
    }

    @Autowired
    private com.aiinterview.backend.repository.InvoiceRepository invoiceRepository;

    @AfterEach
    public void cleanup() {
        if (testUser != null) {
            List<com.aiinterview.backend.entity.Invoice> invoices = invoiceRepository.findAll().stream().filter(i -> i.getUser().getId().equals(testUser.getId())).toList();
            invoiceRepository.deleteAll(invoices);
            List<Subscription> subs = subscriptionRepository.findByUserId(testUser.getId());
            subscriptionRepository.deleteAll(subs);
            List<Payment> payments = paymentRepository.findByUserId(testUser.getId());
            paymentRepository.deleteAll(payments);
            userRepository.delete(testUser);
        }
    }

    @Test
    public void testOrderCreation() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        
        Map<String, Object> response = paymentService.createOrder(testUser, request, true);
        assertNotNull(response.get("cashfreeOrderId"));
        assertNotNull(response.get("paymentSessionId"));
        assertEquals("CREATED", response.get("status"));
    }

    @Test
    public void testAuthRequired() {
        PaymentRequest request = new PaymentRequest();
        assertThrows(SecurityException.class, () -> paymentService.createOrder(null, request, true));
    }

    @Test
    public void testInvalidPlan() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId("99999");
        assertThrows(RuntimeException.class, () -> paymentService.createOrder(testUser, request, true));
    }
    
    @Test
    public void testVerificationAndIdempotency() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        Map<String, Object> response = paymentService.createOrder(testUser, request, true);
        String orderId = (String) response.get("cashfreeOrderId");

        // First verification (Mock placeholder verifies success automatically)
        Map<String, Object> verifyResponse = paymentService.verifyAndActivatePayment(orderId, testUser.getId());
        assertEquals("success", verifyResponse.get("status"));
        assertNotNull(verifyResponse.get("subscriptionId"));

        // Second verification should be idempotent
        Map<String, Object> duplicateResponse = paymentService.verifyAndActivatePayment(orderId, testUser.getId());
        assertEquals("already_success", duplicateResponse.get("status"));
    }

    @Test
    public void testWrongUserOwnership() {
        PaymentRequest request = new PaymentRequest();
        request.setPlanId(String.valueOf(testPlan.getId()));
        Map<String, Object> response = paymentService.createOrder(testUser, request, true);
        String orderId = (String) response.get("cashfreeOrderId");

        User w = new User();
        w.setUsername("wrong_user_" + System.currentTimeMillis());
        w.setEmail("wrong@example.com");
        w.setPassword("pwd");
        User wrongUser = userRepository.save(w);
        try {
            assertThrows(SecurityException.class, () -> paymentService.verifyAndActivatePayment(orderId, wrongUser.getId()));
        } finally {
            userRepository.delete(wrongUser);
        }
    }
}
