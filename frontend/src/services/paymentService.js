import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/payment"
});

const getToken = () => localStorage.getItem("token");

const addAuthToken = (config) => {
    const token = getToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
};

API.interceptors.request.use(addAuthToken);

/**
 * Create a production Razorpay order.
 *
 * The backend decides the actual price.
 * Never send amount from the frontend.
 */
export const createOrder = (
    planId,
    currency = "INR",
    paymentMethod = "razorpay",
    referralCode = null
) =>
    API.post("/create-order", {
        planId,
        currency,
        paymentMethod,
        referralCode
    }).then((res) => res.data);

/**
 * Create a test-mode Razorpay order.
 *
 * Backend controls test pricing:
 * PRO = ₹1
 * ELITE = ₹2
 */
export const createTestOrder = (
    planId,
    currency = "INR",
    paymentMethod = "razorpay",
    referralCode = null
) =>
    API.post("/create-test-order", {
        planId,
        currency,
        paymentMethod,
        referralCode
    }).then((res) => res.data);

/**
 * Verify Razorpay payment.
 *
 * Subscription activation happens on the backend
 * only after successful signature verification.
 */
export const verifyPayment = (payload) =>
    API.post("/verify", payload)
        .then((res) => res.data);

/**
 * Mark a pending payment as failed.
 *
 * Backend verifies that the authenticated user owns
 * the corresponding order.
 */
export const markPaymentFailed = (orderId) =>
    API.post("/mark-failed", {
        razorpay_order_id: orderId
    }).then((res) => res.data);

/**
 * Get authenticated user's payment history.
 */
export const getPaymentHistory = () =>
    API.get("/history")
        .then((res) => res.data);

/*
 * Webhooks are sent by Razorpay directly to the backend.
 * Frontend should NOT call this endpoint.
 *
 * Therefore handleWebhook() has intentionally been removed.
 */