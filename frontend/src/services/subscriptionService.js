import { API_BASE_URL } from "../config";
import axios from "axios";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/subscription"
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

export const getMySubscription = () =>
    API.get("/my").then((res) => res.data);

export const getActiveSubscription = () =>
    API.get("/my/active").then((res) => res.data);

export const checkout = (
    planId,
    currency = "INR",
    paymentMethod = "cashfree",
    referralCode = null
) =>
    API.post("/checkout", {
        planId,
        currency,
        paymentMethod,
        referralCode
    }).then((res) => res.data);

export const subscribe = () =>
    Promise.reject(
        new Error(
            "Direct subscription activation is disabled. Complete the payment first."
        )
    );

export const getCapabilities = () =>
    API.get("/capabilities")
        .then((res) => res.data);