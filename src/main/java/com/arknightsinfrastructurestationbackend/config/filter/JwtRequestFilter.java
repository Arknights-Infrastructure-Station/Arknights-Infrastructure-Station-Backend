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
import org.springframework.util.StringUtils;
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

        // 获取请求路径
        String path = request.getRequestURI();

        // 检查请求路径是否在受保护路径列表中
        boolean isProtectedPath = SecurityPaths.PROTECTED_PATHS.stream().anyMatch(path::startsWith);

        if (!isProtectedPath) {
            // 如果不在受保护路径列表中，直接放行
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        Long uid = null;
        String jwt = null;

        if (authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            uid = jwtUtil.extractUid(jwt); // 从Token中提取uid，能提取是因为uid作为subject参与了Token的生成
        }

        if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = this.selectUserService.getUserByToken(jwt);

            if (user != null && jwtUtil.validateToken(jwt, user)) {
                // Token是有效的
                setSecurityContext(user, request);
            }
        }

        chain.doFilter(request, response);
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
