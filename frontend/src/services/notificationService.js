import axios from "axios";

const API_BASE = "http://localhost:8080/api/notifications";

const getHeaders = () => {
    const token = localStorage.getItem("token");
    if (!token) {
        throw new Error("No authentication token found");
    }
    return {
        Authorization: `Bearer ${token}`
    };
};

export const getUserNotifications = async () => {
    const response = await axios.get(API_BASE, {
        headers: getHeaders()
    });
    return response.data;
};

export const getUnreadNotificationCount = async () => {
    const response = await axios.get(`${API_BASE}/unread-count`, {
        headers: getHeaders()
    });
    return response.data;
};

export const markAllNotificationsAsRead = async () => {
    const response = await axios.post(`${API_BASE}/mark-read`, {}, {
        headers: getHeaders()
    });
    return response.data;
};

export const deleteNotification = async (id) => {
    const response = await axios.delete(`${API_BASE}/${encodeURIComponent(id)}`, {
        headers: getHeaders()
    });
    return response.data;
};

export const clearAllNotifications = async () => {
    const response = await axios.delete(API_BASE, {
        headers: getHeaders()
    });
    return response.data;
};

