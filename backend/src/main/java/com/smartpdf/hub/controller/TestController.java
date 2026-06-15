package com.smartpdf.hub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "This is a public endpoint. Anyone can access it without a JWT token.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secure")
    public ResponseEntity<Map<String, Object>> secureEndpoint(@AuthenticationPrincipal String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "This is a secure endpoint. You have successfully authenticated using Supabase JWT!");
        response.put("supabaseUserId", userId);
        return ResponseEntity.ok(response);
    }
}
