package com.bananabill.repository;

import com.bananabill.model.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpRepository extends MongoRepository<Otp, String> {
    Optional<Otp> findFirstByMobileNumberAndActionOrderByExpiryDesc(String mobileNumber, String action);
}
