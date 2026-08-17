import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080/api/coding"
});

API.interceptors.request.use(
    (config) => {

        const token =
            localStorage.getItem("token");

        if (token) {

            config.headers.Authorization =
                `Bearer ${token}`;

        }

        return config;

    },
    (error) =>
        Promise.reject(error)
);

export const getCodingProblems = () =>
    API.get("/problems");

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

export default API;