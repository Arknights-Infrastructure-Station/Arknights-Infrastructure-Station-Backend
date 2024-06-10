package com.arknightsinfrastructurestationbackend.config.filter;

import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@WebFilter(urlPatterns = "/*")
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60; // 同一IP，1分钟内最多请求次数

    private final Cache<String, AtomicInteger> requestCountsPerIpAddress;

    public RateLimitingFilter() {
        this.requestCountsPerIpAddress = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest
                && response instanceof HttpServletResponse httpServletResponse) {

            String clientIpAddress = getClientIpAddress(httpServletRequest);

            AtomicInteger requestCount;
            try {
                requestCount = requestCountsPerIpAddress.get(clientIpAddress, AtomicInteger::new);
            } catch (ExecutionException e) {
                Log.error(e.getMessage());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误");
                return;
            }

            if (requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                httpServletResponse.setStatus(429);
                httpServletResponse.setContentType("text/plain");
                httpServletResponse.getWriter().write("请求太频繁了");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

