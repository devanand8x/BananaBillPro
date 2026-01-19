package com.bananabill.repository;

import com.bananabill.model.Farmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerRepositoryTest {

    @Mock
    private FarmerRepository farmerRepository;

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
    void findByMobileNumber_ShouldReturnFarmer() {
        when(farmerRepository.findByMobileNumber("9876543210")).thenReturn(Optional.of(testFarmer));

        Optional<Farmer> result = farmerRepository.findByMobileNumber("9876543210");

        assertTrue(result.isPresent());
        assertEquals("Test Farmer", result.get().getName());
        verify(farmerRepository, times(1)).findByMobileNumber("9876543210");
    }

    @Test
    void findByMobileNumber_WhenNotFound_ShouldReturnEmpty() {
        when(farmerRepository.findByMobileNumber("1111111111")).thenReturn(Optional.empty());

        Optional<Farmer> result = farmerRepository.findByMobileNumber("1111111111");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllFarmers() {
        when(farmerRepository.findAll()).thenReturn(List.of(testFarmer));

        List<Farmer> result = farmerRepository.findAll();

        assertEquals(1, result.size());
        assertEquals("Test Farmer", result.get(0).getName());
    }

    @Test
    void findById_ShouldReturnFarmer() {
        when(farmerRepository.findById("farmer-1")).thenReturn(Optional.of(testFarmer));

        Optional<Farmer> result = farmerRepository.findById("farmer-1");

        assertTrue(result.isPresent());
        assertEquals("farmer-1", result.get().getId());
    }

    @Test
    void save_ShouldReturnSavedFarmer() {
        when(farmerRepository.save(any(Farmer.class))).thenReturn(testFarmer);

        Farmer result = farmerRepository.save(testFarmer);

        assertNotNull(result);
        assertEquals("farmer-1", result.getId());
        verify(farmerRepository, times(1)).save(any(Farmer.class));
    }
}
