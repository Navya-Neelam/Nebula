package com.nebula.auth.repository;

import com.nebula.auth.model.PasswordResetOtp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends MongoRepository<PasswordResetOtp, String> {
    Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    Optional<PasswordResetOtp> findByEmailAndOtpHashAndUsedFalse(String email, String otpHash);
}
