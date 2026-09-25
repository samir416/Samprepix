import { API_BASE_URL } from "../config";
import axios from "axios";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/support"
});

API.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

/**
 * Submit a problem / bug report to admin
 * @param {Object} data - { feature, description, actionAttempted, reporterEmail, pageUrl }
 */
export const submitProblemReport = async (data) => {
    const payload = {
        ...data,
        pageUrl: data.pageUrl || window.location.pathname + window.location.search
    };
    const response = await API.post("/report-problem", payload);
    return response.data;
};

/**
 * Ask a platform question to the AI Support Assistant
 * @param {string} question
 */
export const askSupportQuestion = async (question) => {
    const response = await API.post("/ask", { question });
    return response.data;
};

/**
 * Dispatches global window event to open Report Problem modal from anywhere
 * @param {Object} [options] - { feature, description, actionAttempted }
 */
export const openReportProblemModal = (options = {}) => {
    window.dispatchEvent(new CustomEvent("open-report-problem", {
        detail: {
            feature: options.feature || "General",
            description: options.description || "",
            actionAttempted: options.actionAttempted || "",
            pageUrl: options.pageUrl || window.location.pathname
        }
    }));
};
