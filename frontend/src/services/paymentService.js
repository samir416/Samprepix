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

export const createOrder = (planId, currency, paymentMethod) =>
    API.post("/create-order", { planId, currency, paymentMethod }).then(res => res.data);
export const createTestOrder = (planId, currency, paymentMethod) =>
    API.post("/create-test-order", { planId, currency, paymentMethod }).then(res => res.data);
export const verifyPayment = (payload) =>
    API.post("/verify", payload).then(res => res.data);
export const markPaymentFailed = (orderId) =>
    API.post("/mark-failed", { razorpay_order_id: orderId }).then(res => res.data);
export const getPaymentHistory = () => API.get("/history").then(res => res.data);
export const handleWebhook = (payload) =>
    API.post("/webhook", payload).then(res => res.data);