import { API_BASE_URL } from "../config";
import axios from "axios";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/ai-roadmap"
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

export const getAiRoadmap = () =>
    API.get("").then((res) => res.data);

export const switchRoadmapTrack = (trackId, customTitle) =>
    API.post("/track", { trackId, customTitle }).then((res) => res.data);

export const toggleRoadmapMilestone = (trackId, milestoneId) =>
    API.post(`/milestone/${trackId}/${milestoneId}/toggle`).then((res) => res.data);

export const getTrackSuggestions = (query = "") =>
    API.get(`/suggestions?q=${encodeURIComponent(query)}`).then((res) => res.data);
