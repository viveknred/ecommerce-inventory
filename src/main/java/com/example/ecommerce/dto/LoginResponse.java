package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Role;

public class LoginResponse {

    private String token;
    private String email;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String email, Role role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}