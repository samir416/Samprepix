import axios from "axios";

const API =
    "http://localhost:8080/api/profile";

const GITHUB_API =
    "http://localhost:8080/api/github";

export const getCleanToken = () => {
    const raw = localStorage.getItem("token");
    if (!raw || raw === "null" || raw === "undefined" || !raw.trim()) {
        return null;
    }
    let cleaned = raw.replace(/^"|"$/g, "").trim();
    if (cleaned.toLowerCase().startsWith("bearer ")) {
        cleaned = cleaned.substring(7).trim();
    }
    return cleaned || null;
};

export const getAuthConfig = () => {
    const token = getCleanToken();
    if (!token) {
        throw new Error("No authentication token found. Please sign in.");
    }
    return {
        headers: {
            Authorization: `Bearer ${token}`
        }
    };
};

export async function getProfile() {

    const response =
        await axios.get(
            API,
            getAuthConfig()
        );

    return response.data;
}

export async function updateProfile(data) {

    const config = getAuthConfig();
    const response =
        await axios.put(
            API,
            data,
            {
                ...config,
                headers: {
                    ...config.headers,
                    "Content-Type": "application/json"
                }
            }
        );

    return response.data;
}

export const uploadProfilePicture =
    async (file) => {

        const formData =
            new FormData();

        formData.append(
            "file",
            file
        );

        const response =
            await fetch(
                `${API}/upload-photo`,
                {
                    method: "POST",
                    headers: {
                        Authorization:
                            `Bearer ${getToken()}`
                    },
                    body: formData
                }
            );

        if (!response.ok) {

            throw new Error(
                "Failed to upload profile picture"
            );
        }

        return await response.text();
    };

export const removeProfilePicture =
    async () => {

        await axios.delete(
            `${API}/remove-photo`,
            getAuthConfig()
        );
    };

export const getSkillSuggestions =
    async (
        role,
        query,
        signal
    ) => {

        const response =
            await axios.get(
                `${API}/skills/suggestions`,
                {
                    params: {
                        role,
                        query
                    },
                    signal,
                    headers: {
                        Authorization:
                            `Bearer ${getToken()}`
                    }
                }
            );

        return response.data;
    };

export const getGitHubRepository =
    async () => {

        const response =
            await axios.get(
                `${GITHUB_API}/repository`,
                getAuthConfig()
            );

        return response.data;
    };

export const getGitHubRepositories =
    async () => {
        const response = await axios.get(
            `${GITHUB_API}/repositories`,
            getAuthConfig()
        );

        return response.data;
    };

export const saveGitHubRepository =
    async (repositoryUrl) => {
        const response = await axios.post(
            `${GITHUB_API}/repository`,
            { repositoryUrl },
            getAuthConfig()
        );

        return response.data;
    };