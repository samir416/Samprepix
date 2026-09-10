import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/subscription"
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

export const getMySubscription = () => API.get("/my").then(res => res.data);
export const getActiveSubscription = () => API.get("/my/active").then(res => res.data);
export const checkout = (planId, currency, paymentMethod) =>
    API.post("/checkout", { planId, currency, paymentMethod }).then(res => res.data);
export const subscribe = (planId, currency, paymentMethod) =>
    API.post("/subscribe", { planId, currency, paymentMethod }).then(res => res.data);
export const cancelSubscription = (subscriptionId) =>
    API.post(`/cancel/${subscriptionId}`).then(res => res.data);
export const getCapabilities = () => API.get("/capabilities").then(res => res.data);
export const getAllSubscriptions = () =>
    API.get("/admin/all").then(res => res.data);