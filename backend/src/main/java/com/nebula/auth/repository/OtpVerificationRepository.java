package com.nebula.auth.repository;

import com.nebula.auth.model.OtpVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends MongoRepository<OtpVerification, String> {
    Optional<OtpVerification> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, String type);
    void deleteByEmail(String email);
}
