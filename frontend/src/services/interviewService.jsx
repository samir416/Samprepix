import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/interview"
});

API.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export const startInterview = (data) =>
    API.post("/start", data);

export const submitAnswer = (data) =>
    API.post("/submit", data);

export const getProgress = (sessionId) =>
    API.get(`/progress/${sessionId}`);

export const getResult = (sessionId) =>
    API.get(`/result/${sessionId}`);

export const endInterview = (sessionId) =>
    API.post(`/end/${sessionId}`);

export const getCompletedInterviewCount = () =>
API.get("/count");