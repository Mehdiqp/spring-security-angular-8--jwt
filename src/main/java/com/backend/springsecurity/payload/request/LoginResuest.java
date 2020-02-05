package com.backend.springsecurity.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;

public class LoginResuest {
    private String username;
    private String password;

    public LoginResuest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginResuest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
