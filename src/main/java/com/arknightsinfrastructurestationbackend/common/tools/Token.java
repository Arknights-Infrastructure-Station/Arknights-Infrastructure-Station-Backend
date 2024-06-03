package com.arknightsinfrastructurestationbackend.common.tools;

import jakarta.servlet.http.HttpServletRequest;

public class Token {
    public static String getTokenByRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        return token;
    }
}
