import axios from "axios";

const API_BASE = "http://localhost:8080/api/performance";

export const getPerformanceAnalytics = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error("No authentication token found");
    }

    const response = await axios.get(`${API_BASE}/analytics`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    return response.data;
};
