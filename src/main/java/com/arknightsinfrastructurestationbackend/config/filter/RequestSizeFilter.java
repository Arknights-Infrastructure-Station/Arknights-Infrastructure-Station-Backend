package com.arknightsinfrastructurestationbackend.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@WebFilter(urlPatterns = "/*")
public class RequestSizeFilter implements Filter {

    private static final int MAX_REQUEST_SIZE = 3 * 1024 * 1024; // 3 MB

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if ("POST".equalsIgnoreCase(httpServletRequest.getMethod())) {
                RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);
                if (requestWrapper.getContentLength() > MAX_REQUEST_SIZE) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.setContentType("text/plain");
                    httpServletResponse.getWriter().write("请求携带数据超出限制");
                    return;
                }
                chain.doFilter(requestWrapper, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private static class RequestWrapper extends HttpServletRequestWrapper {

        private final byte[] body;

        public RequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = request.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
            body = byteArrayOutputStream.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new ServletInputStream() {
                private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }
            };
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
