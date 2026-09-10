import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/admin"
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

export const getAdminStats = () => API.get("/stats").then(res => res.data);
export const getAllUsers = (page = 0, size = 20, role, accountStatus) =>
    API.get("/users", {
        params: { page, size, role, accountStatus }
    }).then(res => res.data);
export const getUserById = (id) => API.get(`/users/${id}`).then(res => res.data);
export const updateUserRole = (id, role) =>
    API.put(`/users/${id}/role`, { role }).then(res => res.data);
export const updateUserStatus = (id, accountStatus) =>
    API.put(`/users/${id}/status`, null, {
        params: { accountStatus }
    }).then(res => res.data);
export const deleteUser = (id) => API.delete(`/users/${id}`).then(res => res.data);
export const getUserSubscriptions = (id) =>
    API.get(`/users/${id}/subscriptions`).then(res => res.data);
export const getAllSubscriptions = () =>
    API.get("/subscription/admin/all").then(res => res.data);
export const getUserEntitlements = (id) =>
    API.get(`/users/${id}/entitlements`).then(res => res.data);
export const grantTemporaryEntitlement = (id, data) =>
    API.post(`/users/${id}/entitlements/temporary`, data).then(res => res.data);
export const grantLifetimeEntitlement = (id, data) =>
    API.post(`/users/${id}/entitlements/lifetime`, data).then(res => res.data);
export const revokeEntitlement = (id, entitlementId) =>
    API.delete(`/users/${id}/entitlements/${entitlementId}`).then(res => res.data);
export const revokeAllEntitlements = (id) =>
    API.delete(`/users/${id}/entitlements`).then(res => res.data);
export const getEntitlementHistory = (userId) =>
    API.get(`/entitlements/history/${userId}`).then(res => res.data);
export const getPricing = () => API.get("/plans/pricing").then(res => res.data);
export const getBillingHistory = (userId) =>
    API.get(`/billing/${userId}`).then(res => res.data);