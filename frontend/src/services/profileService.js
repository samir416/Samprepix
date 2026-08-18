import axios from "axios";

const API =
    "http://localhost:8080/api/profile";

const GITHUB_API =
    "http://localhost:8080/api/github";

const getToken = () =>
    localStorage.getItem("token");

const getAuthConfig = () => ({
    headers: {
        Authorization:
            `Bearer ${getToken()}`
    }
});

export async function getProfile() {

    const response =
        await axios.get(
            API,
            getAuthConfig()
        );

    return response.data;
}

export async function updateProfile(data) {

    const response =
        await axios.put(
            API,
            data,
            getAuthConfig()
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