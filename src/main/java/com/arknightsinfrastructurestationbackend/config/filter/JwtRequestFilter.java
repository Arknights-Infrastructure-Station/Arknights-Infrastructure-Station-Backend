package com.arknightsinfrastructurestationbackend.config.filter;

import com.arknightsinfrastructurestationbackend.config.data.SecurityPaths;
import com.arknightsinfrastructurestationbackend.entitiy.user.adminUser.AdminUser;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.global.type.UserType;
import com.arknightsinfrastructurestationbackend.service.user.adminUser.SelectAdminUserService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.SelectUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final SelectUserService selectUserService;
    private final SelectAdminUserService selectAdminUserService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isProtectedPath = SecurityPaths.USER_PATHS.stream().anyMatch(path::startsWith)
                || SecurityPaths.ADMIN_PATHS.stream().anyMatch(path::startsWith);

        if (!isProtectedPath) {
            chain.doFilter(request, response);
            return;
        }

        String requestType = request.getHeader("requestType");
        if (requestType == null) {
            unauthorizedResponse(response, "请求类型缺失");
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            unauthorizedResponse(response, "授权标头缺失或无效");
            return;
        }

        String jwt = authorizationHeader.substring(7);
        Long uid = jwtUtil.extractUid(jwt);
        String userType = jwtUtil.extractUserType(jwt);

        if (uid == null || userType == null) {
            unauthorizedResponse(response, "令牌无效或用户未通过身份验证");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        switch (requestType) {
            case "UserRequest":
                if (!UserType.ORDINARY_USER.getName().equals(userType)) {
                    unauthorizedResponse(response, "请求类型与 Token 类型不匹配");
                    return;
                }
                handleUserRequest(jwt, request, response, chain);
                break;
            case "AdminUserRequest":
                if (!UserType.ADMIN_USER.getName().equals(userType)) {
                    unauthorizedResponse(response, "请求类型与 Token 类型不匹配");
                    return;
                }
                handleAdminRequest(jwt, request, response, chain);
                break;
            default:
                unauthorizedResponse(response, "未知的请求类型");
        }
    }

    private void handleUserRequest(String jwt, HttpServletRequest request,
                                   HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        User user = selectUserService.getByToken(jwt);
        if (user != null && jwtUtil.validateUserToken(jwt, user)) {
            setSecurityContext(user, request);
            chain.doFilter(request, response);
        } else {
            unauthorizedResponse(response, "令牌无效或用户未通过身份验证");
        }
    }

    private void handleAdminRequest(String jwt, HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        AdminUser adminUser = selectAdminUserService.getByToken(jwt);
        if (adminUser != null && jwtUtil.validateAdminToken(jwt, adminUser)) {
            setSecurityContext(adminUser, request);
            chain.doFilter(request, response);
        } else {
            unauthorizedResponse(response, "令牌无效或管理员未通过身份验证");
        }
    }

    private void setSecurityContext(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void setSecurityContext(AdminUser adminUser, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        adminUser, null, adminUser.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void unauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}