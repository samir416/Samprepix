import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/aptitude"
});

const addAuthToken = (config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
};

API.interceptors.request.use(
    (config) => addAuthToken(config),
    (error) => Promise.reject(error)
);

export const getAptitudeQuestions = async (topicId, page = 0, size = 20) => {
    const response = await API.get("/questions", {
        params: { topicId, page, size }
    });
    return response.data;
};

export const getAptitudeQuestionById = async (id) => {
    const response = await API.get(`/questions/${id}`);
    return response.data;
};

export const getAptitudeQuestionsByCategory = async (name, difficulty = "", page = 0, size = 20) => {
    const response = await API.get("/category", {
        params: { name, difficulty, page, size }
    });
    return response.data;
};

export const checkAptitudeAnswer = async (questionId, selectedOption) => {
    const response = await API.post("/check", {
        questionId,
        selectedOption
    });
    return response.data;
};

export const getAptitudeStats = async () => {
    const response = await API.get("/stats");
    return response.data;
};

export const getAssessmentQuestions = async (track = "all", count = 15) => {
    const response = await API.get("/assessment", {
        params: { track, count }
    });
    return response.data;
};

export const submitAssessment = async (submission) => {
    const response = await API.post("/assessment/submit", submission);
    return response.data;
};

export const getAptitudeAttempts = async () => {
    const response = await API.get("/attempts");
    return response.data;
};

export const getAptitudeAttemptById = async (id) => {
    const response = await API.get(`/attempts/${id}`);
    return response.data;
};

export default {
    getAptitudeQuestions,
    getAptitudeQuestionById,
    getAptitudeQuestionsByCategory,
    checkAptitudeAnswer,
    getAptitudeStats,
    getAssessmentQuestions,
    submitAssessment,
    getAptitudeAttempts,
    getAptitudeAttemptById
};
