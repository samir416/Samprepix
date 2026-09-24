import axios from "axios";
import { API_BASE_URL } from "../config";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/payment"
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

export const createOrder = (
    planId,
    currency = "INR",
    paymentMethod = "cashfree",
    referralCode = null
) =>
    API.post("/create-order", {
        planId,
        currency,
        paymentMethod,
        referralCode
    }).then((res) => res.data);

export const createTestOrder = (
    planId,
    currency = "INR",
    paymentMethod = "cashfree",
    referralCode = null
) =>
    API.post("/create-test-order", {
        planId,
        currency,
        paymentMethod,
        referralCode
    }).then((res) => res.data);

export const verifyPayment = (payload) =>
    API.post("/verify", payload)
        .then((res) => res.data);

export const markPaymentFailed = (orderId) =>
    API.post("/mark-failed", {
        order_id: orderId
    }).then((res) => res.data);

export const getPaymentHistory = () =>
    API.get("/history")
        .then((res) => res.data);