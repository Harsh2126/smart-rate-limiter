package com.example.smart_rate_limiter.controller;
import com.example.smart_rate_limiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import java.util.*;


@RestController
@CrossOrigin(origins = "*")
public class ApiController {
    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/api/check")
    public ResponseEntity<?> checkRateLimit(
            @RequestParam(required = false) String clientId,
            HttpServletRequest request) {

        String finalClientId = (clientId != null && !clientId.isBlank())
                ? clientId
                : getClientIp(request);

        boolean allowed = rateLimiterService.AllowedRequest(finalClientId);
        if (allowed) {
            return ResponseEntity.ok(Map.of("status", "allowed", "clientId", finalClientId));
        } else {
            return ResponseEntity.status(429).body(Map.of("status", "blocked", "message", "Rate limit exceeded", "clientId", finalClientId));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    @GetMapping("/api/stats")
    public ResponseEntity<?> getStats(@RequestParam String clientId) {
        return ResponseEntity.ok(rateLimiterService.getStats(clientId));
    }


    
}
