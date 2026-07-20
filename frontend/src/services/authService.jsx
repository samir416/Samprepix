import axios from "axios";

const API_URL = "http://localhost:8080";

export const loginUser = async (email, password) => {

    const response = await axios.post(
        `${API_URL}/login`,
        {
            email,
            password
        }
    );

    return response.data;
};

export const registerUser = async (
    username,
    email,
    password
) => {

    const response = await axios.post(
        `${API_URL}/register`,
        {
            username,
            email,
            password
        }
    );

    return response.data;
};