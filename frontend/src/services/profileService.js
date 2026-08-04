import axios from "axios";

const API =
    "http://localhost:8080/api/profile";


    export async function getProfile() {

    const token =
        localStorage.getItem("token");

    const response =
        await axios.get(
            API,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

    return response.data;

}


export async function updateProfile(data) {

    const token =
        localStorage.getItem("token");

    const response =
        await axios.put(
            API,
            data,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

    return response.data;

}

export const uploadProfilePicture = async (file) => {

    const token = localStorage.getItem("token");

    const formData = new FormData();

    formData.append("file", file);

    const response = await fetch(
        "http://localhost:8080/api/profile/upload-photo",
        {
            method: "POST",
            headers: {
                Authorization: `Bearer ${token}`
            },
            body: formData
        }
    );

    if (!response.ok) {
        throw new Error("Failed to upload profile picture");
    }

    return await response.text();
};

export const removeProfilePicture = async () => {

    const token = localStorage.getItem("token");

 await axios.delete(
    `${API}/remove-photo`,
    {
        headers: {
            Authorization: `Bearer ${token}`
        }
    }
);

};


export const getSkillSuggestions = async (role, query, signal) => {

    const token = localStorage.getItem("token");

    const response = await axios.get(

        `${API}/skills/suggestions`,

        {

            params: {

                role,

                query

            },

            signal,

            headers: {

                Authorization: `Bearer ${token}`

            }

        }

    );

    return response.data;

};

