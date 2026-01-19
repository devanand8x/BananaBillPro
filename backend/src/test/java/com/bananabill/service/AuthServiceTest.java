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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        // Set up test data
        mockUser = new User();
        mockUser.setId("user123");
        mockUser.setName("Test User");
        mockUser.setMobileNumber("9876543210");
        mockUser.setEmail("9876543210@bananabill.app");
        mockUser.setPassword("encodedPassword");

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("Test User");
        validRegisterRequest.setMobile("9876543210");
        validRegisterRequest.setPassword("Password123!");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setMobile("9876543210");
        validLoginRequest.setPassword("Password123!");
    }

    @Test
    void register_WithValidData_ShouldCreateUserAndReturnToken() {
        // Arrange
        when(userRepository.existsByMobileNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(new Profile());
        when(tokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(tokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(anyString(), anyString())).thenReturn(refreshToken);

        // Act
        TokenResponse response = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    void register_WithExistingMobile_ShouldThrowValidationException() {
        // Arrange
        when(userRepository.existsByMobileNumber(anyString())).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        assertEquals("This mobile number is already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithInvalidMobile_ShouldThrowValidationException() {
        // Arrange
        validRegisterRequest.setMobile("123"); // Invalid mobile

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithWeakPassword_ShouldThrowValidationException() {
        // Arrange
        validRegisterRequest.setPassword("weak"); // Weak password

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithEmptyName_ShouldThrowValidationException() {
        // Arrange
        validRegisterRequest.setName("");

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> authService.register(validRegisterRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(tokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        when(refreshTokenService.createRefreshToken(anyString(), anyString())).thenReturn(refreshToken);

        // Act
        TokenResponse response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository).findByMobileNumber("9876543210");
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowValidationException() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void login_WithWrongPassword_ShouldThrowValidationException() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void getUserByMobile_WithExistingUser_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.of(mockUser));

        // Act
        UserDTO result = authService.getUserByMobile("9876543210");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("9876543210", result.getMobileNumber());
    }

    @Test
    void getUserByMobile_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> authService.getUserByMobile("9876543210"));
    }

    @Test
    void updatePasswordByMobile_WithValidData_ShouldUpdatePassword() {
        // Arrange
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        authService.updatePasswordByMobile("9876543210", "NewPassword123!");

        // Assert
        verify(userRepository).save(mockUser);
        verify(refreshTokenService).revokeAllUserTokens("user123");
        assertEquals("newEncodedPassword", mockUser.getPassword());
    }

    @Test
    void updatePasswordByMobile_WithWeakPassword_ShouldThrowValidationException() {
        // Act & Assert
        assertThrows(ValidationException.class,
                () -> authService.updatePasswordByMobile("9876543210", "weak"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void logout_ShouldRevokeRefreshToken() {
        // Act
        authService.logout("refresh-token");

        // Assert
        verify(refreshTokenService).revokeToken("refresh-token");
    }

    @Test
    void logoutAll_ShouldRevokeAllUserTokens() {
        // Act
        authService.logoutAll("user123");

        // Assert
        verify(refreshTokenService).revokeAllUserTokens("user123");
    }
}
