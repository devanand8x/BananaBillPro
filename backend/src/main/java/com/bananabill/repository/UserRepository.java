package com.bananabill.repository;

import com.bananabill.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByEmail(String email);

    Boolean existsByMobileNumber(String mobileNumber);

    Boolean existsByEmail(String email);
}
