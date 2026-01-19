package com.bananabill.service;

import com.bananabill.dto.LoginRequest;
import com.bananabill.dto.RegisterRequest;
import com.bananabill.dto.TokenResponse;
import com.bananabill.dto.UserDTO;
import com.bananabill.exception.ValidationException;
import com.bananabill.model.Profile;
import com.bananabill.model.RefreshToken;
import com.bananabill.model.User;
import com.bananabill.repository.ProfileRepository;
import com.bananabill.repository.UserRepository;
import com.bananabill.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 * Handles user registration, login, and token management
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Register new user
     */
    public TokenResponse register(RegisterRequest request) {
        // Input validation
        validateMobileNumber(request.getMobile());
        validatePassword(request.getPassword());
        validateName(request.getName());

        // Check if user already exists
        if (userRepository.existsByMobileNumber(request.getMobile())) {
            logger.warn("Registration attempt with existing mobile: {}", maskMobile(request.getMobile()));
            throw new ValidationException("This mobile number is already registered");
        }

        // Create email from mobile
        String email = request.getMobile() + "@bananabill.app";

        // Create user
        User user = new User(
                request.getName().trim(),
                request.getMobile(),
                email,
                passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // Create profile
        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setName(request.getName().trim());
        profile.setMobileNumber(request.getMobile());
        profileRepository.save(profile);

        logger.info("New user registered: {}", maskMobile(request.getMobile()));

        // Generate tokens
        return generateTokenResponse(user);
    }

    /**
     * User login
     */
    public TokenResponse login(LoginRequest request) {
        // Input validation
        validateMobileNumber(request.getMobile());

        // Find user by mobile
        User user = userRepository.findByMobileNumber(request.getMobile())
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", maskMobile(request.getMobile()));
                    return new ValidationException("Invalid credentials");
                });

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed - wrong password: {}", maskMobile(request.getMobile()));
            throw new ValidationException("Invalid credentials");
        }

        logger.info("User logged in: {}", maskMobile(request.getMobile()));

        // Generate tokens
        return generateTokenResponse(user);
    }

    /**
     * Get user by mobile
     */
    public UserDTO getUserByMobile(String mobile) {
        User user = userRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDTO(user.getId(), user.getName(),
                user.getMobileNumber(), user.getEmail());
    }

    /**
     * Update password
     */
    public void updatePasswordByMobile(String mobile, String newPassword) {
        validatePassword(newPassword);

        User user = userRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenService.revokeAllUserTokens(user.getId());

        logger.info("Password updated for user: {}", maskMobile(mobile));
    }

    /**
     * Logout - revoke refresh token
     */
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    /**
     * Logout from all devices
     */
    public void logoutAll(String userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }

    // ========== HELPER METHODS ==========

    private TokenResponse generateTokenResponse(User user) {
        // Generate access token
        String accessToken = tokenProvider.generateAccessToken(
                user.getMobileNumber(), user.getId());

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(), user.getMobileNumber());

        // Create user DTO
        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getName(),
                user.getMobileNumber(),
                user.getEmail());

        // Calculate expiry in seconds
        long expiresIn = tokenProvider.getAccessTokenExpiration() / 1000;

        return new TokenResponse(accessToken, refreshToken.getToken(), expiresIn, userDTO);
    }

    private void validateMobileNumber(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            throw new ValidationException("Mobile number is required");
        }
        // Use security validator for strict mobile validation
        if (!com.bananabill.security.MobileValidator.isValid(mobile)) {
            throw new ValidationException(
                    "Invalid mobile number. Use 10-digit Indian mobile number starting with 6, 7, 8, or 9");
        }
    }

    private void validatePassword(String password) {
        // Use security validator for strong password policy
        var result = com.bananabill.security.PasswordValidator.validate(password);
        if (!result.isValid()) {
            throw new ValidationException(result.error());
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("Name must be at least 2 characters");
        }
        if (name.length() > 100) {
            throw new ValidationException("Name is too long");
        }
    }

    private String maskMobile(String mobile) {
        return com.bananabill.security.MobileValidator.mask(mobile);
    }
}
