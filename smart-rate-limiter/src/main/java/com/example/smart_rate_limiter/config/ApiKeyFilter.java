package com.example.smart_rate_limiter.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyFilter implements Filter {

    @Value("${apikey.valid-keys}")
    private String validKeysRaw;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Sirf /api/ wale endpoints protect karo, OPTIONS preflight ko skip karo
        if (path.startsWith("/api/") && !httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            String apiKey = httpRequest.getHeader("X-API-Key");
            List<String> validKeys = Arrays.asList(validKeysRaw.split(","));

            if (apiKey == null || !validKeys.contains(apiKey)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"status\":\"unauthorized\",\"message\":\"Missing or invalid API key\"}");
                return; // request ko aage nahi jaane denge
            }
        }

        chain.doFilter(request, response); // sab sahi hai, request aage badhne do
    }
}