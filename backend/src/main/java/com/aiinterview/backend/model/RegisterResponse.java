package com.aiinterview.backend.model;

public class RegisterResponse {

    private boolean success;
    private String message;
    private String email;
    private String token;

    public RegisterResponse() {
    }

    public RegisterResponse(
            boolean success,
            String message,
            String email) {

        this.success = success;
        this.message = message;
        this.email = email;
    }

    public RegisterResponse(
            boolean success,
            String message,
            String email,
            String token) {

        this.success = success;
        this.message = message;
        this.email = email;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}