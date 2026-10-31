package com.aiinterview.backend;

import com.aiinterview.backend.dto.subscription.UpgradeQuoteResponse;
import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import com.aiinterview.backend.service.PaymentService;
import com.aiinterview.backend.service.SubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CashfreePaymentUpgradeAndAutoPayTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SubscriptionService subscriptionService;

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

    @Autowired
    private UpgradeTransactionRepository upgradeTransactionRepository;

    private User testUser;
    private Plan proPlan;
    private Plan elitePlan;

    @BeforeEach
    public void setup() {
        long timestamp = System.currentTimeMillis();

        testUser = new User();
        testUser.setName("Test AutoPay User");
        testUser.setUsername("test_cf_upgrade_" + timestamp);
        testUser.setEmail("test_upgrade_" + timestamp + "@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        proPlan = planRepository
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

        elitePlan = planRepository
                .findByNameIgnoreCase("ELITE")
                .orElseGet(() -> planRepository.save(
                        Plan.builder()
                                .name("ELITE")
                                .description("Elite Plan")
                                .priceInr(799.0)
                                .priceUsd(19.0)
                                .interval("MONTH")
                                .maxMockInterviews(999)
                                .maxResumeScans(999)
                                .maxCodingProblems(999)
                                .maxAptitudeQuestions(999)
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

        upgradeTransactionRepository.deleteAll(upgradeTransactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()));

        List<Invoice> invoices = invoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getUser() != null && testUser.getId().equals(invoice.getUser().getId()))
                .toList();
        invoiceRepository.deleteAll(invoices);

        List<Subscription> subscriptions = subscriptionRepository.findByUserId(testUser.getId());
        subscriptionRepository.deleteAll(subscriptions);

        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        paymentRepository.deleteAll(payments);

        userRepository.delete(testUser);
    }

    @Test
    public void testUpgradeQuoteCalculation() {
        LocalDateTime originalExpiry = LocalDateTime.now().plusDays(20);

        Subscription sub = Subscription.builder()
                .user(testUser)
                .plan(proPlan)
                .subscriptionStatus("ACTIVE")
                .amountPaid(399.0)
                .currency("INR")
                .subscribedAt(LocalDateTime.now().minusDays(10))
                .expiresAt(originalExpiry)
                .autoRenew(true)
                .nextRenewalDate(originalExpiry)
                .refundEligible(false)
                .upgradeCount(0)
                .build();
        subscriptionRepository.save(sub);

        UpgradeQuoteResponse quote = subscriptionService.getUpgradeQuote(testUser.getId());

        assertNotNull(quote);
        assertTrue(quote.isEligible());
        assertEquals("PRO", quote.getCurrentPlan());
        assertEquals("ELITE", quote.getTargetPlan());
        assertEquals(400.0, quote.getUpgradeAmount(), 0.01);
        assertTrue(quote.getRemainingDays() >= 19 && quote.getRemainingDays() <= 21);
    }

    @Test
    public void testUpgradePreservesOriginalExpiresAt() {
        LocalDateTime originalExpiry = LocalDateTime.now().plusDays(15);

        Subscription sub = Subscription.builder()
                .user(testUser)
                .plan(proPlan)
                .subscriptionStatus("ACTIVE")
                .amountPaid(399.0)
                .currency("INR")
                .subscribedAt(LocalDateTime.now().minusDays(15))
                .expiresAt(originalExpiry)
                .autoRenew(true)
                .nextRenewalDate(originalExpiry)
                .refundEligible(false)
                .upgradeCount(0)
                .build();
        sub = subscriptionRepository.save(sub);

        Payment upgradePayment = Payment.builder()
                .user(testUser)
                .plan(elitePlan)
                .cashfreeOrderId("CF_UPG_" + System.currentTimeMillis())
                .paymentMethod("CASHFREE")
                .amount(400.0)
                .displayAmount(400.0)
                .currency("INR")
                .paymentStatus("PENDING")
                .paymentType("UPGRADE")
                .upgradeFromPlan("PRO")
                .upgradeToPlan("ELITE")
                .originalPaidAmount(399.0)
                .targetPlanPrice(799.0)
                .originalExpiresAt(originalExpiry)
                .autoRenew(true)
                .build();
        upgradePayment = paymentRepository.save(upgradePayment);

        UpgradeTransaction upgTx = UpgradeTransaction.builder()
                .user(testUser)
                .subscription(sub)
                .previousPlan("PRO")
                .targetPlan("ELITE")
                .originalPaidAmount(399.0)
                .targetPlanPrice(799.0)
                .upgradeAmount(400.0)
                .subscriptionExpiry(originalExpiry)
                .cashfreeOrderId(upgradePayment.getCashfreeOrderId())
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        upgradeTransactionRepository.save(upgTx);

        // Activate upgrade entitlements through public endpoint delegate
        paymentService.activateEntitlements(upgradePayment, upgradePayment.getCashfreeOrderId());

        // Verify updated subscription
        Subscription updatedSub = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertEquals("ELITE", updatedSub.getPlan().getName().toUpperCase());
        assertEquals(1, updatedSub.getUpgradeCount());
        // STRICT REQUIREMENT: Preserves original expiry date!
        assertEquals(originalExpiry.toLocalDate(), updatedSub.getExpiresAt().toLocalDate());
        assertFalse(updatedSub.isRefundEligible());

        // Verify upgrade transaction marked SUCCESS
        List<UpgradeTransaction> txs = upgradeTransactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertFalse(txs.isEmpty());
        assertEquals("SUCCESS", txs.get(0).getPaymentStatus());
    }

    @Test
    public void testEliteUserNotEligibleForUpgrade() {
        Subscription sub = Subscription.builder()
                .user(testUser)
                .plan(elitePlan)
                .subscriptionStatus("ACTIVE")
                .amountPaid(799.0)
                .currency("INR")
                .subscribedAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(25))
                .autoRenew(true)
                .refundEligible(false)
                .upgradeCount(0)
                .build();
        subscriptionRepository.save(sub);

        UpgradeQuoteResponse quote = subscriptionService.getUpgradeQuote(testUser.getId());
        assertNotNull(quote);
        assertFalse(quote.isEligible());
        assertEquals("You are already on the highest tier (Elite).", quote.getReason());
    }

    @Test
    public void testAutoPayToggleKeepsPlanActiveAndDoesNotRefund() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(25);

        Subscription sub = Subscription.builder()
                .user(testUser)
                .plan(proPlan)
                .subscriptionStatus("ACTIVE")
                .amountPaid(399.0)
                .currency("INR")
                .subscribedAt(LocalDateTime.now().minusDays(5))
                .expiresAt(expiry)
                .autoRenew(true)
                .nextRenewalDate(expiry)
                .refundEligible(false)
                .upgradeCount(0)
                .build();
        sub = subscriptionRepository.save(sub);

        // Turn OFF AutoPay
        Subscription offSub = subscriptionService.setAutoRenew(sub.getId(), testUser.getId(), false);
        assertFalse(offSub.isAutoRenew());
        assertNull(offSub.getNextRenewalDate());
        assertEquals("ACTIVE", offSub.getSubscriptionStatus());
        assertEquals(expiry.toLocalDate(), offSub.getExpiresAt().toLocalDate());
        assertFalse(offSub.isRefundEligible());

        // Turn ON AutoPay
        Subscription onSub = subscriptionService.setAutoRenew(sub.getId(), testUser.getId(), true);
        assertTrue(onSub.isAutoRenew());
        assertNotNull(onSub.getNextRenewalDate());
        assertEquals(expiry.toLocalDate(), onSub.getNextRenewalDate().toLocalDate());
        assertEquals("ACTIVE", onSub.getSubscriptionStatus());
    }
}
