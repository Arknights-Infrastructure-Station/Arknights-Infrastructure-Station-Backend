package com.arknightsinfrastructurestationbackend.config.filter;

import com.arknightsinfrastructurestationbackend.config.data.SecurityPaths;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
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

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isProtectedPath = SecurityPaths.USER_PATHS.stream().anyMatch(path::startsWith);

        if (!isProtectedPath) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // Deny access if Authorization header is missing or invalid
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"授权标头缺失或无效\"}");
            return;
        }

        String jwt = authorizationHeader.substring(7);
        Long uid = jwtUtil.extractUid(jwt);

        if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = selectUserService.getUserByToken(jwt);

            if (user != null && jwtUtil.validateToken(jwt, user)) {
                setSecurityContext(user, request);
                chain.doFilter(request, response);
                return;
            }
        }

        // Deny access if token validation fails or uid is null
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"message\": \"令牌无效或用户未通过身份验证\"}");
    }


    private void setSecurityContext(User user, HttpServletRequest request) {
        // 将用户的权限写入到安全上下文中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
