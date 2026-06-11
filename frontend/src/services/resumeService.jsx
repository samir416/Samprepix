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

    export const uploadResume =
    async (file) => {

        const token =
            localStorage.getItem(
                "token"
            );

        const formData =
            new FormData();

        formData.append(
            "file",
            file
        );

        const response =
            await axios.post(

                "http://localhost:8080/resume/upload",

                formData,

                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        return response.data;
    };

    export const analyzeResumeFile =
    async (file) => {

        const token =
            localStorage.getItem(
                "token"
            );

        const formData =
            new FormData();

        formData.append(
            "file",
            file
        );

        const response =
            await axios.post(

                `${API_URL}/resume/analyze-file`,

                formData,

                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        return response.data;
    };

    export const getResumeHistory =
    async () => {

        const token =
            localStorage.getItem(
                "token"
            );

        const response =
            await axios.get(

                `${API_URL}/resume/history`,

                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        return response.data;
    };
    