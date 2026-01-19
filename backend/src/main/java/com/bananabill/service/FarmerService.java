package com.bananabill.service;

import com.bananabill.dto.FarmerRequest;
import com.bananabill.exception.ResourceNotFoundException;
import com.bananabill.model.Farmer;
import com.bananabill.model.User;
import com.bananabill.repository.FarmerRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public Farmer findByMobile(String mobile) {
        return farmerRepository.findByMobileNumber(mobile).orElse(null);
    }

    public Farmer upsertFarmer(FarmerRequest request) {
        String cleanMobile = request.getMobileNumber().replaceAll("\\D", "");

        // Get current user
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        // Check if farmer exists
        Farmer farmer = farmerRepository.findByMobileNumber(cleanMobile).orElse(null);

        if (farmer != null) {
            // Update existing farmer
            farmer.setName(request.getName());
            farmer.setAddress(request.getAddress());
        } else {
            // Create new farmer
            farmer = new Farmer();
            farmer.setMobileNumber(cleanMobile);
            farmer.setName(request.getName());
            farmer.setAddress(request.getAddress());
            farmer.setCreatedBy(currentUser.getId());
        }

        return farmerRepository.save(farmer);
    }

    public Farmer getFarmerById(String id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
    }

    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }
}
