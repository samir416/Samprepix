import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/coding"
});

const ROOT_API = axios.create({
    baseURL: "http://localhost:8080/api"
});

const addAuthToken = (config) => {
    const token =
        localStorage.getItem("token");

    if (token) {
        config.headers.Authorization =
            `Bearer ${token}`;
    }

    return config;
};

API.interceptors.request.use(
    (config) => addAuthToken(config),
    (error) =>
        Promise.reject(error)
);

ROOT_API.interceptors.request.use(
    (config) => addAuthToken(config),
    (error) =>
        Promise.reject(error)
);

export const getCodingProblems = (
    page = 0,
    size = 50,
    search = "",
    difficulty = "",
    tag = ""
) =>
    API.get("/problems", {
        params: {
            page,
            size,
            search: search || undefined,
            difficulty: difficulty || undefined,
            tag: tag || undefined
        }
    });

export const getCodingProblemTags = () =>
    API.get("/problems/tags");

export const getCodingProblem = (
    problemId
) =>
    API.get(
        `/problems/${problemId}`
    );

export const getCodingProblemsByDifficulty = (
    difficulty
) =>
    API.get(
        `/problems/difficulty/${encodeURIComponent(
            difficulty
        )}`
    );

export const getCodingProgress = () =>
    API.get("/progress");

export const selectCodingProblem = (
    problemId
) =>
    API.put(
        `/progress/problem/${problemId}`
    );

export const saveLastSelectedProblem = (
    problemId
) =>
    API.put(
        `/progress/last-selected/${problemId}`
    );

export const saveCodingState = (
    problemId,
    language,
    code
) =>
    API.put(
        `/progress/code/${problemId}`,
        code,
        {
            params: {
                language
            },
            headers: {
                "Content-Type":
                    "text/plain"
            }
        }
    );

export const completeCodingProblem = (
    problemId
) =>
    API.put(
        `/progress/complete/${problemId}`
    );

export const updateCodingSubmission = (
    successful
) =>
    API.put(
        "/progress/submission",
        null,
        {
            params: {
                successful
            }
        }
    );

export const executeCodingCode = (
    problemId,
    language,
    code
) =>
    API.post(
        "/execute",
        {
            problemId,
            language,
            code
        }
    );

export const submitCodingCode = (
    problemId,
    language,
    code
) =>
    API.post(
        "/submit",
        {
            problemId,
            language,
            code
        }
    );

export const getCodingHint = (
    problemTitle,
    problemDescription,
    language,
    code
) =>
    ROOT_API.post(
        "/ai/coding-hint",
        {
            problemTitle,
            problemDescription,
            language,
            code
        }
    );

export const getCodingRuntimes = () =>
    API.get("/runtimes");

export default API;
