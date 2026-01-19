package com.bananabill.service;

import com.bananabill.dto.FarmerRequest;
import com.bananabill.exception.ResourceNotFoundException;
import com.bananabill.model.Farmer;
import com.bananabill.model.User;
import com.bananabill.repository.FarmerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private FarmerService farmerService;

    private Farmer testFarmer;

    @BeforeEach
    void setUp() {
        testFarmer = new Farmer();
        testFarmer.setId("farmer-1");
        testFarmer.setName("Test Farmer");
        testFarmer.setMobileNumber("9876543210");
        testFarmer.setAddress("Test Address");
    }

    @Test
    void findByMobile_WhenFarmerExists_ShouldReturnFarmer() {
        when(farmerRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(testFarmer));

        Farmer result = farmerService.findByMobile("9876543210");

        assertNotNull(result);
        assertEquals("Test Farmer", result.getName());
        verify(farmerRepository, times(1)).findByMobileNumber("9876543210");
    }

    @Test
    void findByMobile_WhenFarmerNotExists_ShouldReturnNull() {
        when(farmerRepository.findByMobileNumber("1111111111")).thenReturn(Optional.empty());

        Farmer result = farmerService.findByMobile("1111111111");

        assertNull(result);
        verify(farmerRepository, times(1)).findByMobileNumber("1111111111");
    }

    @Test
    void getFarmerById_WhenFarmerExists_ShouldReturnFarmer() {
        when(farmerRepository.findById("farmer-1")).thenReturn(Optional.of(testFarmer));

        Farmer result = farmerService.getFarmerById("farmer-1");

        assertNotNull(result);
        assertEquals("farmer-1", result.getId());
    }

    @Test
    void getFarmerById_WhenFarmerNotExists_ShouldThrowException() {
        when(farmerRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            farmerService.getFarmerById("non-existent");
        });
    }

    @Test
    void getAllFarmers_ShouldReturnAllFarmers() {
        when(farmerRepository.findAll()).thenReturn(List.of(testFarmer));

        List<Farmer> result = farmerService.getAllFarmers();

        assertEquals(1, result.size());
        assertEquals("Test Farmer", result.get(0).getName());
    }

    @Test
    void getAllFarmers_WhenEmpty_ShouldReturnEmptyList() {
        when(farmerRepository.findAll()).thenReturn(List.of());

        List<Farmer> result = farmerService.getAllFarmers();

        assertTrue(result.isEmpty());
    }
}
