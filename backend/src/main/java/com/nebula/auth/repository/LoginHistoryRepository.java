package com.nebula.auth.repository;

import com.nebula.auth.model.LoginHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends MongoRepository<LoginHistory, String> {
    List<LoginHistory> findByEmailOrderByLoginTimeDesc(String email);
    List<LoginHistory> findTop20ByEmailOrderByLoginTimeDesc(String email);
}
