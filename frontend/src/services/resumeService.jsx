import axios from "axios";

const API_URL =
    "http://localhost:8080";

export const getResumeAnalysis =
    async () => {

        const token =
            localStorage.getItem(
                "token"
            );

        const response =
            await axios.get(

                `${API_URL}/resume/analyze`,

                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        return response.data;
    };