package com.bananabill.controller;

import com.bananabill.dto.FarmerRequest;
import com.bananabill.dto.response.ApiResponse;
import com.bananabill.exception.ResourceNotFoundException;
import com.bananabill.model.Farmer;
import com.bananabill.service.FarmerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Farmer Controller
 * Manages farmer CRUD operations
 */
@RestController
@RequestMapping("/farmers")
public class FarmerController {

    private static final Logger logger = LoggerFactory.getLogger(FarmerController.class);

    private final FarmerService farmerService;

    // Constructor injection
    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    /**
     * Get farmer by mobile number
     * GET /api/farmers/mobile/{mobile}
     */
    @GetMapping("/mobile/{mobile}")
    public ResponseEntity<ApiResponse<Farmer>> getFarmerByMobile(@PathVariable String mobile) {
        logger.debug("Fetching farmer by mobile: ******{}", mobile.substring(Math.max(0, mobile.length() - 4)));

        Farmer farmer = farmerService.findByMobile(mobile);
        if (farmer == null) {
            throw new ResourceNotFoundException("Farmer", "mobile", mobile);
        }

        return ResponseEntity.ok(ApiResponse.success(farmer));
    }

    /**
     * Create or update farmer
     * POST /api/farmers
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Farmer>> createOrUpdateFarmer(@Valid @RequestBody FarmerRequest request) {
        logger.info("Creating/updating farmer: {}", request.getName());

        Farmer farmer = farmerService.upsertFarmer(request);

        return ResponseEntity.ok(ApiResponse.success("Farmer saved successfully", farmer));
    }

    /**
     * Get all farmers for current user
     * GET /api/farmers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Farmer>>> getAllFarmers() {
        logger.debug("Fetching all farmers");

        List<Farmer> farmers = farmerService.getAllFarmers();

        return ResponseEntity.ok(ApiResponse.success(farmers));
    }

    /**
     * Get farmer by ID
     * GET /api/farmers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Farmer>> getFarmerById(@PathVariable String id) {
        logger.debug("Fetching farmer by ID: {}", id);

        Farmer farmer = farmerService.getFarmerById(id);

        return ResponseEntity.ok(ApiResponse.success(farmer));
    }
}
