package com.bananabill.repository;

import com.bananabill.model.Farmer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmerRepository extends MongoRepository<Farmer, String> {

    Optional<Farmer> findByMobileNumber(String mobileNumber);

    List<Farmer> findByCreatedBy(String userId);

    Boolean existsByMobileNumber(String mobileNumber);
}
