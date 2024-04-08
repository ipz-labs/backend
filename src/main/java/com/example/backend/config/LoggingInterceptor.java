package com.example.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Log request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", request.getMethod());
        requestDetails.put("path", request.getRequestURI());
        requestDetails.put("params", request.getParameterMap());
        logMessage("Request", requestDetails);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Log response details
        Map<String, Object> responseDetails = new HashMap<>();
        responseDetails.put("status", response.getStatus());
        logMessage("Response", responseDetails);
    }

    private void logMessage(String label, Map<String, Object> details) {
        try {
            String jsonString = objectMapper.writeValueAsString(details);
            System.out.println(label + ": " + jsonString);
        } catch (Exception e) {
            System.err.println("Error logging " + label + ": " + e.getMessage());
        }
    }
}