package com.nebula.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "courses")
public class Course {

    @Id
    private String id;
    private String title;
    private String description;
    private String imageUrl; // For backward compatibility with existing front-end images
    private String thumbnail; // Matches requirement: thumbnail
    private String category;
    private String duration;
    private Double rating;
    private String instructor; // Name of instructor
    private String instructorId; // ID of instructor user
    private String detailDescription;
    private Double price;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String status = "PUBLISHED"; // PUBLISHED or DRAFT

    public Course() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = "PUBLISHED";
    }

    public Course(String title, String description, String imageUrl, String category, String duration, Double rating, String instructor, String detailDescription, Double price) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.thumbnail = imageUrl;
        this.category = category;
        this.duration = duration;
        this.rating = rating;
        this.instructor = instructor;
        this.detailDescription = detailDescription;
        this.price = price;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = "PUBLISHED";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.thumbnail = imageUrl;
    }

    public String getThumbnail() {
        return thumbnail != null ? thumbnail : imageUrl;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        this.imageUrl = thumbnail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public void setDetailDescription(String detailDescription) {
        this.detailDescription = detailDescription;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
