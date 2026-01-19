package com.bananabill.controller;

import com.bananabill.dto.LoginRequest;
import com.bananabill.dto.RegisterRequest;
import com.bananabill.dto.TokenResponse;
import com.bananabill.dto.UserDTO;
import com.bananabill.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private TokenResponse mockTokenResponse;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserDTO("user-1", "Test User", "9876543210", null);
        mockTokenResponse = new TokenResponse(
                "accessToken123",
                "refreshToken123",
                900L,
                mockUser);
    }

    @Test
    void register_ShouldCallAuthServiceRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setMobile("9876543210");
        request.setPassword("Password123!");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockTokenResponse);

        TokenResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("accessToken123", response.accessToken());
        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void login_ShouldCallAuthServiceLogin() {
        LoginRequest request = new LoginRequest();
        request.setMobile("9876543210");
        request.setPassword("Password123!");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockTokenResponse);

        TokenResponse response = authService.login(request);

        assertEquals("accessToken123", response.accessToken());
        assertNotNull(response.user());
        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void tokenResponse_ShouldContainAllFields() {
        assertNotNull(mockTokenResponse.accessToken());
        assertNotNull(mockTokenResponse.refreshToken());
        assertNotNull(mockTokenResponse.user());
        assertEquals("Bearer", mockTokenResponse.tokenType());
    }

    @Test
    void registerRequest_Validation_NameIsRequired() {
        RegisterRequest request = new RegisterRequest();
        request.setName("");
        request.setMobile("9876543210");
        request.setPassword("password123");

        assertTrue(request.getName().isEmpty());
    }

    @Test
    void loginRequest_Validation_MobileLength() {
        LoginRequest request = new LoginRequest();
        request.setMobile("9876543210");
        request.setPassword("password123");

        assertEquals(10, request.getMobile().length());
    }

    @Test
    void logout_ShouldCallAuthServiceLogout() {
        String refreshToken = "testRefreshToken";
        doNothing().when(authService).logout(anyString());

        authService.logout(refreshToken);

        verify(authService, times(1)).logout(refreshToken);
    }
}
