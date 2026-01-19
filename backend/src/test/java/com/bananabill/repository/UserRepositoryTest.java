package com.bananabill.repository;

import com.bananabill.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-1");
        testUser.setName("Test User");
        testUser.setMobileNumber("9876543210");
        testUser.setPassword("hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByMobileNumber_ShouldReturnUser() {
        when(userRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(testUser));

        Optional<User> result = userRepository.findByMobileNumber("9876543210");

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
        verify(userRepository, times(1)).findByMobileNumber("9876543210");
    }

    @Test
    void findByMobileNumber_WhenNotFound_ShouldReturnEmpty() {
        when(userRepository.findByMobileNumber("1111111111")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByMobileNumber("1111111111");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByMobileNumber_ShouldReturnTrue() {
        when(userRepository.existsByMobileNumber("9876543210")).thenReturn(true);

        Boolean exists = userRepository.existsByMobileNumber("9876543210");

        assertTrue(exists);
    }

    @Test
    void existsByMobileNumber_WhenNotExists_ShouldReturnFalse() {
        when(userRepository.existsByMobileNumber("1111111111")).thenReturn(false);

        Boolean exists = userRepository.existsByMobileNumber("1111111111");

        assertFalse(exists);
    }

    @Test
    void save_ShouldReturnSavedUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userRepository.save(testUser);

        assertNotNull(result);
        assertEquals("user-1", result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
