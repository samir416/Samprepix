import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/feedback"
});

API.interceptors.request.use((config) => {

    const token = localStorage.getItem("token");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

/**
 * Submit Interview Feedback
 */
export const submitFeedback = (data) =>
    API.post("/submit", data);