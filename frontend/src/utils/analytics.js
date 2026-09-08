/**
 * Centralized Google Analytics 4 Architecture
 *
 * Prepared for real GA4 tracking once a Measurement ID is configured
 * via VITE_GA_MEASUREMENT_ID environment variable or runtime window.GA_MEASUREMENT_ID.
 *
 * Security & Privacy:
 * - Never sends tokens, passwords, JWTs, API keys, source code, resumes, or PII.
 * - Gracefully no-ops when no Measurement ID is present or if blocked by client.
 * - Does not fabricate fake analytics numbers or user traffic.
 */

const BLOCKED_PARAM_KEYWORDS = [
    "password",
    "token",
    "jwt",
    "secret",
    "auth",
    "key",
    "code",
    "resume",
    "answer",
    "credential",
    "bearer",
    "email"
];

let isInitialized = false;

/**
 * Sanitize event parameters to prevent PII or sensitive state leaks.
 */
function sanitizeParams(params) {
    if (!params || typeof params !== "object") return {};
    const sanitized = {};
    for (const [key, value] of Object.entries(params)) {
        const lowerKey = key.toLowerCase();
        const isBlocked = BLOCKED_PARAM_KEYWORDS.some((kw) => lowerKey.includes(kw));
        if (!isBlocked && typeof value !== "function" && typeof value !== "object") {
            sanitized[key] = value;
        }
    }
    return sanitized;
}

/**
 * Initialize GA4 script dynamically if a valid Measurement ID is configured.
 */
export function initGA() {
    if (isInitialized || typeof window === "undefined") return;

    const measurementId =
        import.meta.env.VITE_GA_MEASUREMENT_ID ||
        window.GA_MEASUREMENT_ID ||
        null;

    if (!measurementId || typeof measurementId !== "string" || !measurementId.startsWith("G-")) {
        // No valid ID configured yet; remain dormant without error
        return;
    }

    try {
        const script = document.createElement("script");
        script.async = true;
        script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
        document.head.appendChild(script);

        window.dataLayer = window.dataLayer || [];
        function gtag() {
            window.dataLayer.push(arguments);
        }
        window.gtag = gtag;

        gtag("js", new Date());
        gtag("config", measurementId, {
            send_page_view: false // Managed manually via SPA route listener
        });

        isInitialized = true;
    } catch (err) {
        // Fail silently; analytics must never break the host application
    }
}

/**
 * Track SPA Page View
 */
export function trackPageView(pagePath, pageTitle) {
    if (typeof window === "undefined") return;

    try {
        if (window.gtag && typeof window.gtag === "function") {
            window.gtag("event", "page_view", {
                page_path: pagePath,
                page_title: pageTitle || document.title,
                page_location: window.location.href
            });
        }
    } catch (_) {
        // Fail silently
    }
}

/**
 * Track Public User Interaction Event
 */
export function trackEvent(eventName, params = {}) {
    if (typeof window === "undefined" || !eventName) return;

    try {
        if (window.gtag && typeof window.gtag === "function") {
            window.gtag("event", eventName, sanitizeParams(params));
        }
    } catch (_) {
        // Fail silently
    }
}

export default {
    initGA,
    trackPageView,
    trackEvent
};
