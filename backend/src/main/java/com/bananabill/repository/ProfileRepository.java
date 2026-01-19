package com.bananabill.repository;

import com.bananabill.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {

    Optional<Profile> findByUserId(String userId);

    Optional<Profile> findByMobileNumber(String mobileNumber);
}
