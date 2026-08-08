package com.nebula.auth.controller;

import com.nebula.auth.model.Course;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.CourseEnrollmentRepository;
import com.nebula.auth.repository.CourseRepository;
import com.nebula.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseEnrollmentRepository enrollmentRepository;

    private Course sampleCourse;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course("Generative AI Basics", "Intro to AI", "http://img.png", "Generative AI", "5 hours", 4.8, "Clara", "Details", 49.99);
        sampleCourse.setId("course123");

        sampleUser = new User("Jane Student", "student@nebula.com", "pass", null);
        sampleUser.setId("user123");
        sampleUser.setRole("STUDENT");
    }

    @Test
    @WithMockUser(username = "student@nebula.com")
    void testGetCourseByIdSuccess() throws Exception {
        Mockito.when(courseRepository.findById("course123")).thenReturn(Optional.of(sampleCourse));

        mockMvc.perform(get("/api/courses/course123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Generative AI Basics"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    @WithMockUser(username = "student@nebula.com")
    void testEnrollInCourseSuccess() throws Exception {
        Mockito.when(userRepository.findByEmail("student@nebula.com")).thenReturn(Optional.of(sampleUser));
        Mockito.when(courseRepository.findById("course123")).thenReturn(Optional.of(sampleCourse));
        Mockito.when(enrollmentRepository.existsByUserIdAndCourseId("user123", "course123")).thenReturn(false);

        mockMvc.perform(post("/api/courses/course123/enroll"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Enrolled successfully"))
                .andExpect(jsonPath("$.courseId").value("course123"));
    }
}
