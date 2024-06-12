package com.abel.ecommerce.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class JwtRespone implements Serializable {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String email;

    public JwtRespone(String accessToken,
            String refreshToken,
            String username,
            String email) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.email = email;
    }
}
