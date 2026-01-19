package com.bananabill.controller;

import com.bananabill.service.AuthService;
import com.bananabill.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class OtpController {

    private final OtpService otpService;
    private final AuthService authService;

    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        try {
            String mobile = body.get("mobile");
            String action = body.getOrDefault("action", "login");
            if (mobile == null || mobile.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "mobile is required"));
            }

            boolean ok = otpService.sendOtp(mobile, action);
            if (ok)
                return ResponseEntity.ok(Map.of("success", true));
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send OTP"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        try {
            String mobile = body.get("mobile");
            String otp = body.get("otp");
            String action = body.getOrDefault("action", "login");

            if (mobile == null || otp == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "mobile and otp are required"));
            }

            var tokenOpt = otpService.verifyOtp(mobile, otp, action);
            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
            }

            String token = tokenOpt.orElseThrow(() -> new IllegalStateException("Token should not be empty"));

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("token", token);
            // Provide an action_link similar to Supabase so frontend can reuse
            // ResetPassword flow
            String actionLink = String.format("/reset-password#access_token=%s", token);
            resp.put("action_link", actionLink);

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
