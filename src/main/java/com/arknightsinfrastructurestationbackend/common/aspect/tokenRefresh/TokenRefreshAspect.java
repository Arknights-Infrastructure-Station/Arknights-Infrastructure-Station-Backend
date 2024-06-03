package com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh;

import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.config.JWTUtil;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Objects;

@Aspect
@Component
@AllArgsConstructor
public class TokenRefreshAspect {
    private UserService userService;
    private SelectUserService selectUserService;
    private final JWTUtil jwtUtil;

    // 定义拦截点，拦截所有Controller层的方法
    @Pointcut("(within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *))" +
            "&& !(@within(com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh) || @annotation(com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh))")
    public void controllerMethods() {
    }

    // 定义通知，在Controller方法返回后执行，尝试刷新用户的Token
    @AfterReturning("controllerMethods()")
    public void afterControllerMethod(JoinPoint joinPoint) {
        HttpServletRequest request = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                request = (HttpServletRequest) arg;
                break;
            }
        }

        if (request != null) {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            if (response != null && !response.isCommitted()) {
                // 检查Token
                String token = Token.getTokenByRequest(request);
                User user = selectUserService.getUserByToken(token);
                if (jwtUtil.isTokenExpiringWithin(token, Duration.ofDays(7))) {
                    // 若Token将在一周内过期，重新生成
                    String newToken = jwtUtil.generateUniqueToken(user.getId()); // 根据uid生成新Token
                    userService.updateUserToken(user.getId(), newToken); // 更新Token

                    // 将新Token添加到响应头
                    response.setHeader("Authorization", "Bearer " + newToken);
                }
            }
        }
    }
}