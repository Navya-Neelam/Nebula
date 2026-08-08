package com.nebula.auth.repository;

import com.nebula.auth.model.CourseEnrollment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends MongoRepository<CourseEnrollment, String> {
    List<CourseEnrollment> findByUserId(String userId);
    Optional<CourseEnrollment> findByUserIdAndCourseId(String userId, String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
}
