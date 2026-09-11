import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/admin/plans"
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

// ==================== PLANS ====================

export const getAllPlans = () =>
    API.get("/").then((res) => res.data);

export const getActivePlans = () =>
    API.get("/active").then((res) => res.data);

export const getPlanById = (id) =>
    API.get(`/${id}`).then((res) => res.data);

export const createPlan = (planData) =>
    API.post("/", planData).then((res) => res.data);

export const updatePlan = (id, planData) =>
    API.put(`/${id}`, planData).then((res) => res.data);

export const deletePlan = (id) =>
    API.delete(`/${id}`).then((res) => res.data);