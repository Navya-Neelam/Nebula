package com.nebula.auth.repository;

import com.nebula.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByEmail(String email);
    List<RefreshToken> findByEmailAndRevokedFalse(String email);
    void deleteByToken(String token);
    void deleteByEmail(String email);
}
