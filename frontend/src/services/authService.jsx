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

export const forgotPassword = async (email) => {

    const response = await axios.post(
        `${API_URL}/forgot-password`,
        {
            email
        }
    );

    return response.data;

};

export const resetPassword = async (
    token,
    password
) => {

    const response = await axios.post(
        `${API_URL}/reset-password`,
        {
            token,
            password
        }
    );

    return response.data;

};

export const verifyOtp = async (email, otp) => {

    const response = await axios.post(
        `${API_URL}/verify-otp`,
        {
            email,
            otp
        }
    );

    return response.data;
};

export const resendOtp = async (email) => {

    const response = await axios.post(
        `${API_URL}/resend-otp`,
        {
            email
        }
    );

    return response.data;
};

export const getCurrentUser = async () => {

    const token = localStorage.getItem("token");

    const response = await axios.get(
        `${API_URL}/me`,
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};