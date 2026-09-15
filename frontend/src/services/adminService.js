import { API_BASE_URL } from "../config";
import axios from "axios";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/admin"
});

const ROOT_API = axios.create({
    baseURL: API_BASE_URL + "/api"
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
ROOT_API.interceptors.request.use(addAuthToken);

const handleUnauthorized = (error) => {
    if (error.response && error.response.status === 401) {
        if (typeof window !== "undefined" && window.location && window.location.pathname.startsWith("/admin")) {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            window.location.href = "/login";
        }
    }
    return Promise.reject(error);
};

API.interceptors.response.use((res) => res, handleUnauthorized);
ROOT_API.interceptors.response.use((res) => res, handleUnauthorized);

// =========================================================
// DASHBOARD
// =========================================================

export const getAdminStats = () =>
    API.get("/stats")
        .then((res) => res.data);

// =========================================================
// USERS
// =========================================================

export const getAllUsers = (
    page = 0,
    size = 20,
    role,
    accountStatus
) =>
    API.get("/users", {
        params: {
            page,
            size,
            ...(role ? { role } : {}),
            ...(accountStatus ? { accountStatus } : {})
        }
    }).then((res) => res.data);

export const getUserById = (id) =>
    API.get(`/users/${id}`)
        .then((res) => res.data);

export const updateUserRole = (id, role) =>
    API.put(`/users/${id}/role`, { role })
        .then((res) => res.data);

export const updateUserStatus = (id, accountStatus) =>
    API.put(`/users/${id}/status`, null, {
        params: { accountStatus }
    }).then((res) => res.data);

export const deleteUser = (id) =>
    API.delete(`/users/${id}`)
        .then((res) => res.data);

// =========================================================
// USER SUBSCRIPTIONS
// =========================================================

export const getUserSubscriptions = (id) =>
    API.get(`/users/${id}/subscriptions`)
        .then((res) => res.data);

// =========================================================
// ALL SUBSCRIPTIONS
// =========================================================

export const getAllSubscriptions = () =>
    ROOT_API.get("/subscription/admin/all")
        .then((res) => res.data);

// =========================================================
// ENTITLEMENTS
// =========================================================

export const getUserEntitlements = (id) =>
    API.get(`/users/${id}/entitlements`)
        .then((res) => res.data);

export const grantTemporaryEntitlement = (id, data) =>
    API.post(
        `/users/${id}/entitlements/temporary`,
        data
    ).then((res) => res.data);

export const grantLifetimeEntitlement = (id, data) =>
    API.post(
        `/users/${id}/entitlements/lifetime`,
        data
    ).then((res) => res.data);

export const revokeEntitlement = (id, entitlementId) =>
    API.delete(
        `/users/${id}/entitlements/${entitlementId}`
    ).then((res) => res.data);

export const revokeAllEntitlements = (id) =>
    API.delete(`/users/${id}/entitlements`)
        .then((res) => res.data);

export const getEntitlementHistory = (userId) =>
    API.get(`/entitlements/history/${userId}`)
        .then((res) => res.data);

// =========================================================
// BILLING
// =========================================================

export const getBillingHistory = (userId) =>
    API.get(`/billing/${userId}`)
        .then((res) => res.data);