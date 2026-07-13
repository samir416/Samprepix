import axios from "axios";

const API_URL = "http://localhost:8080";

/**
 * Analyze Resume
 */
export const analyzeResumeFile = async (file) => {

    const token = localStorage.getItem("token");

    const formData = new FormData();

    formData.append("file", file);

    const response = await axios.post(

        `${API_URL}/resume/analyze`,

        formData,

        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};

/**
 * Resume History
 */
export const getResumeHistory = async () => {

    const token = localStorage.getItem("token");

    const response = await axios.get(

        `${API_URL}/resume/history`,

        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};

/**
 * Latest Resume Analysis
 */
export const getLatestResumeAnalysis = async () => {

    const token = localStorage.getItem("token");

    const response = await axios.get(

        `${API_URL}/resume/latest`,

        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};