package com.nebula.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "course_enrollments")
@CompoundIndexes({
    @CompoundIndex(name = "user_course_idx", def = "{'userId': 1, 'courseId': 1}", unique = true)
})
public class CourseEnrollment {

    @Id
    private String id;

    private String userId;
    private String courseId;
    private LocalDateTime enrolledAt;
    private Double pricePaid;
    private String status = "ACTIVE"; // ACTIVE, CANCELLED, COMPLETED

    public CourseEnrollment() {
    }

    public CourseEnrollment(String userId, String courseId, Double pricePaid) {
        this.userId = userId;
        this.courseId = courseId;
        this.pricePaid = pricePaid;
        this.enrolledAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Double getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(Double pricePaid) {
        this.pricePaid = pricePaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
