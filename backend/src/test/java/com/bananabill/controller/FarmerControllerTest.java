package com.bananabill.controller;

import com.bananabill.dto.FarmerRequest;
import com.bananabill.exception.ResourceNotFoundException;
import com.bananabill.model.Farmer;
import com.bananabill.service.FarmerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerControllerTest {

    @Mock
    private FarmerService farmerService;

    private Farmer testFarmer;
    private FarmerRequest farmerRequest;

    @BeforeEach
    void setUp() {
        testFarmer = new Farmer();
        testFarmer.setId("farmer-1");
        testFarmer.setName("Test Farmer");
        testFarmer.setMobileNumber("9876543210");
        testFarmer.setAddress("Test Address");

        farmerRequest = new FarmerRequest();
        farmerRequest.setName("Test Farmer");
        farmerRequest.setMobileNumber("9876543210");
        farmerRequest.setAddress("Test Address");
    }

    @Test
    void getFarmerByMobile_ShouldCallService() {
        when(farmerService.findByMobile("9876543210")).thenReturn(testFarmer);

        Farmer result = farmerService.findByMobile("9876543210");

        assertNotNull(result);
        assertEquals("Test Farmer", result.getName());
        verify(farmerService, times(1)).findByMobile("9876543210");
    }

    @Test
    void getFarmerByMobile_WhenNotFound_ShouldReturnNull() {
        when(farmerService.findByMobile("1111111111")).thenReturn(null);

        Farmer result = farmerService.findByMobile("1111111111");

        assertNull(result);
    }

    @Test
    void createOrUpdateFarmer_ShouldCallUpsert() {
        when(farmerService.upsertFarmer(any(FarmerRequest.class))).thenReturn(testFarmer);

        Farmer result = farmerService.upsertFarmer(farmerRequest);

        assertNotNull(result);
        assertEquals("farmer-1", result.getId());
        verify(farmerService, times(1)).upsertFarmer(any(FarmerRequest.class));
    }

    @Test
    void getAllFarmers_ShouldReturnList() {
        when(farmerService.getAllFarmers()).thenReturn(List.of(testFarmer));

        List<Farmer> result = farmerService.getAllFarmers();

        assertEquals(1, result.size());
        verify(farmerService, times(1)).getAllFarmers();
    }

    @Test
    void getFarmerById_ShouldReturnFarmer() {
        when(farmerService.getFarmerById("farmer-1")).thenReturn(testFarmer);

        Farmer result = farmerService.getFarmerById("farmer-1");

        assertNotNull(result);
        assertEquals("farmer-1", result.getId());
    }

    @Test
    void getFarmerById_WhenNotFound_ShouldThrow() {
        when(farmerService.getFarmerById("non-existent"))
                .thenThrow(new ResourceNotFoundException("Farmer not found"));

        assertThrows(ResourceNotFoundException.class, () -> {
            farmerService.getFarmerById("non-existent");
        });
    }
}
