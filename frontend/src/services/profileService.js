import axios from "axios";

const API =
    "http://localhost:8080/api/profile";

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