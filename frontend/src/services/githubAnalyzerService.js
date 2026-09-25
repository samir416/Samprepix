import { API_BASE_URL } from "../config";
import axios from "axios";

const API = axios.create({
    baseURL: API_BASE_URL + "/api/github-analyzer"
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

export const analyzeGithubProfile = (profileUrl) =>
    API.post("/analyze", { profileUrl }).then((res) => res.data);

export const getLatestGithubAnalysis = () =>
    API.get("/latest").then((res) => res.data);

export const applyReadmeToGithub = (username, readmeContent) =>
    API.post("/apply-readme", { username, readmeContent }).then((res) => res.data);
