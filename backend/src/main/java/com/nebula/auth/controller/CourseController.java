package com.nebula.auth.controller;

import com.nebula.auth.model.Course;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.CourseRepository;
import com.nebula.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.nebula.auth.model.CourseEnrollment;
import com.nebula.auth.repository.CourseEnrollmentRepository;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public CourseController(CourseRepository courseRepository, UserRepository userRepository, CourseEnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String instructorId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Query query = new Query();
        List<Criteria> criterias = new ArrayList<>();

        // Search: title, instructor, category
        if (search != null && !search.trim().isEmpty()) {
            String regex = ".*" + Pattern.quote(search.trim()) + ".*";
            criterias.add(new Criteria().orOperator(
                Criteria.where("title").regex(regex, "i"),
                Criteria.where("instructor").regex(regex, "i"),
                Criteria.where("category").regex(regex, "i")
            ));
        }

        // Category filter
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
            criterias.add(Criteria.where("category").is(category.trim()));
        }

        // Instructor filter
        if (instructorId != null && !instructorId.trim().isEmpty()) {
            criterias.add(Criteria.where("instructorId").is(instructorId.trim()));
        }

        // Price range filter
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(maxPrice);
            }
            criterias.add(priceCriteria);
        }

        // Rating filter
        if (minRating != null) {
            criterias.add(Criteria.where("rating").gte(minRating));
        }

        // Duration filter
        if (duration != null && !duration.trim().isEmpty()) {
            criterias.add(Criteria.where("duration").is(duration.trim()));
        }

        if (!criterias.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
        }

        long totalItems = mongoTemplate.count(query, Course.class);

        // Sorting
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy.trim()) {
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "createdDate");
                    break;
                case "highestRated":
                    sort = Sort.by(Sort.Direction.DESC, "rating");
                    break;
                case "priceLowToHigh":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "priceHighToLow":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
            }
        }
        query.with(sort);

        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Course> courses = mongoTemplate.find(query, Course.class);

        Map<String, Object> response = new HashMap<>();
        response.put("courses", courses);
        response.put("currentPage", page);
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / size));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return ResponseEntity.ok(course);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ADMIN".equals(user.getRole()) && !"INSTRUCTOR".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only instructors or admins can create courses"));
        }

        course.setInstructorId(user.getId());
        if (course.getInstructor() == null || course.getInstructor().isBlank()) {
            course.setInstructor(user.getFullName());
        }
        course.setCreatedDate(LocalDateTime.now());
        course.setUpdatedDate(LocalDateTime.now());
        if (course.getRating() == null) {
            course.setRating(5.0);
        }

        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable String id, @RequestBody Course courseDetails) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Instructors can only update their own courses
        if ("INSTRUCTOR".equals(user.getRole()) && !user.getId().equals(course.getInstructorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only update your own courses"));
        } else if (!"ADMIN".equals(user.getRole()) && !"INSTRUCTOR".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only instructors or admins can update courses"));
        }

        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setDetailDescription(courseDetails.getDetailDescription());
        course.setCategory(courseDetails.getCategory());
        course.setPrice(courseDetails.getPrice());
        course.setDuration(courseDetails.getDuration());
        if (courseDetails.getThumbnail() != null) {
            course.setThumbnail(courseDetails.getThumbnail());
        }
        if (courseDetails.getStatus() != null) {
            course.setStatus(courseDetails.getStatus());
        }
        course.setUpdatedDate(LocalDateTime.now());

        Course updatedCourse = courseRepository.save(course);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Instructors can only delete their own courses
        if ("INSTRUCTOR".equals(user.getRole()) && !user.getId().equals(course.getInstructorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only delete your own courses"));
        } else if (!"ADMIN".equals(user.getRole()) && !"INSTRUCTOR".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Only instructors or admins can delete courses"));
        }

        courseRepository.delete(course);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<?> enrollInCourse(@PathVariable String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "You are already enrolled in this course"));
        }

        CourseEnrollment enrollment = new CourseEnrollment(user.getId(), course.getId(), course.getPrice());
        enrollmentRepository.save(enrollment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Enrolled successfully", "courseId", course.getId(), "courseTitle", course.getTitle()));
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<Course>> getEnrolledCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        List<String> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).toList();

        List<Course> courses = new ArrayList<>();
        if (!courseIds.isEmpty()) {
            courses = (List<Course>) courseRepository.findAllById(courseIds);
        }

        return ResponseEntity.ok(courses);
    }
}
