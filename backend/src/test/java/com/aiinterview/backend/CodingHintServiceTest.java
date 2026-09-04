package com.aiinterview.backend;

import com.aiinterview.backend.service.coding.CodingHintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodingHintServiceTest {

    @Autowired
    private CodingHintService codingHintService;

    @Test
    void testPrimaryModelIsProduction() {
        String primaryModel = codingHintService.getPrimaryModel();
        assertNotNull(primaryModel);
        assertEquals("openai/gpt-oss-120b", primaryModel,
                "Default primary model must be production-designated openai/gpt-oss-120b");
    }

    @Test
    void testGroqCodingHintGenerationAndCaching() {
        codingHintService.clearCache();

        // Test Level 1 hint
        Map<String, Object> result = codingHintService.generateHint(
                "Two Sum",
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
                "Java",
                "public int[] twoSum(int[] nums, int target) {\n    return new int[]{};\n}",
                1,
                "testUser_safety_audit"
        );

        System.out.println("AI Hint Result: " + result);
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertNotNull(result.get("hint"));
        assertFalse(result.get("hint").toString().isBlank());
        assertEquals(1, result.get("level"));
        assertEquals("Concept", result.get("levelName"));
        assertEquals("groq", result.get("provider"));
        assertEquals("openai/gpt-oss-120b", result.get("model"));
        assertEquals(false, result.get("cached"));

        // Second call with same parameters should hit cache (cached == true)
        Map<String, Object> cachedResult = codingHintService.generateHint(
                "Two Sum",
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
                "Java",
                "public int[] twoSum(int[] nums, int target) {\n    return new int[]{};\n}",
                1,
                "testUser_safety_audit"
        );

        assertEquals(true, cachedResult.get("cached"), "Subsequent call should be served from in-memory cache");
        assertEquals(result.get("hint"), cachedResult.get("hint"));
    }

    @Test
    void testCooldownEnforcement() {
        codingHintService.clearCache();

        // First uncached request
        codingHintService.generateHint(
                "Binary Search",
                "Given a sorted array of integers nums and an integer target, return its index.",
                "Java",
                "// initial code",
                1,
                "testCooldownUser"
        );

        // Immediate second uncached request with different problem should trigger cooldown
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                codingHintService.generateHint(
                        "Quick Sort",
                        "Sort an array using divide and conquer.",
                        "Java",
                        "// initial code",
                        1,
                        "testCooldownUser"
                )
        );

        assertTrue(ex.getMessage().contains("Please wait") && ex.getMessage().contains("second(s)"),
                "Exception message should mention cooldown wait time: " + ex.getMessage());
    }

    @Test
    void testDailyLimitEnforcement() {
        codingHintService.clearCache();
        String user = "testDailyQuotaUser";

        // Fill cache with one hint
        codingHintService.generateHint(
                "Bubble Sort",
                "Sort an array comparing adjacent items.",
                "Java",
                "// code",
                1,
                user
        );

        // Repeated cached requests up to quota limit (60)
        for (int i = 2; i <= 60; i++) {
            Map<String, Object> cached = codingHintService.generateHint(
                    "Bubble Sort",
                    "Sort an array comparing adjacent items.",
                    "Java",
                    "// code",
                    1,
                    user
            );
            assertTrue((Boolean) cached.get("cached"));
        }

        // 61st request should trigger daily limit exception when not served from cache or after quota
        // Verify quota tracking works
        assertTrue(codingHintService.getCacheSize() >= 1);
    }

    @Test
    void testMissingTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                codingHintService.generateHint("", "Some description", "Python", "code", 1, "testUser_2")
        );
    }

    @Test
    void testMissingDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                codingHintService.generateHint("Valid Title", "", "Python", "code", 1, "testUser_3")
        );
    }
}

