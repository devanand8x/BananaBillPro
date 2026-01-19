package com.bananabill.controller;

import com.bananabill.dto.*;
import com.bananabill.dto.response.ApiResponse;
import com.bananabill.exception.ValidationException;
import com.bananabill.model.User;
import com.bananabill.service.AuthService;
import com.bananabill.service.RefreshTokenService;
import com.bananabill.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 * Handles registration, login, token refresh, and logout
 * 
 * Token Strategy:
 * - Access Token: 15 min expiry, used for API calls
 * - Refresh Token: 7 days expiry, used to get new access tokens
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(
            AuthService authService,
            RefreshTokenService refreshTokenService,
            JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register new user
     * POST /api/auth/register
     * Returns: accessToken, refreshToken, user info
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for mobile: ******{}",
                request.getMobile().substring(Math.max(0, request.getMobile().length() - 4)));

        TokenResponse response = authService.register(request);

        logger.info("User registered successfully");
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    /**
     * User login
     * POST /api/auth/login
     * Returns: accessToken, refreshToken, user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for mobile: ******{}",
                request.getMobile().substring(Math.max(0, request.getMobile().length() - 4)));

        TokenResponse response = authService.login(request);

        logger.info("User logged in successfully");
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     * Returns: new accessToken, new refreshToken (rotation)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        logger.debug("Token refresh attempt");

        Map<String, String> tokens = refreshTokenService.refreshTokens(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", tokens));
    }

    /**
     * Logout - revoke current refresh token
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        logger.info("User logged out");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Logout from all devices - revoke all refresh tokens
     * POST /api/auth/logout-all
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        authService.logoutAll(user.getId());

        logger.info("User logged out from all devices");
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices"));
    }

    /**
     * Get current authenticated user
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getName(),
                user.getMobileNumber(),
                user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * Get active session count
     * GET /api/auth/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getActiveSessions() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        long count = refreshTokenService.getActiveSessionCount(user.getId());

        return ResponseEntity.ok(ApiResponse.success(Map.of("activeSessions", count)));
    }

    /**
     * Update password (after OTP verification)
     * POST /api/auth/update-password
     */
    @PostMapping("/update-password")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> updatePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, String> body) {

        // Validate authorization header
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ValidationException("Authorization header is required");
        }

        String token = authorization.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ValidationException("Invalid or expired token");
        }

        // Validate password - strong password rules
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters");
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new ValidationException("password", "Password must contain at least one uppercase letter (A-Z)");
        }
        if (!newPassword.matches(".*[a-z].*")) {
            throw new ValidationException("password", "Password must contain at least one lowercase letter (a-z)");
        }
        if (!newPassword.matches(".*[0-9].*")) {
            throw new ValidationException("password", "Password must contain at least one number (0-9)");
        }
        if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new ValidationException("password",
                    "Password must contain at least one special character (!@#$%^&*)");
        }

        String mobile = jwtTokenProvider.getMobileNumberFromToken(token);
        authService.updatePasswordByMobile(mobile, newPassword);

        logger.info("Password updated for user: ******{}", mobile.substring(Math.max(0, mobile.length() - 4)));

        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", Map.of("success", true)));
    }
}
