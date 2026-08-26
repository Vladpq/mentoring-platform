package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.auth.dto.AuthResponse;
import com.github.valdpq.mentoringplatform.auth.dto.LoginRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.lesson.dto.UpdateLessonStatusRequest;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.review.dto.CreateReviewRequest;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
public class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MentorRepository mentorRepository;

    private String mentorToken;
    private String studentToken;
    private Long mentorId;

    @BeforeEach
    void setUp() throws Exception {
        User mentorUser = User.builder()
                .email("mentor@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MENTOR)
                .build();
        userRepository.save(mentorUser);

        Mentor mentor = Mentor.builder()
                .user(mentorUser)
                .firstName("Test")
                .lastName("Mentor")
                .specialization("Specialization")
                .bio("Bio")
                .pricePerHour(BigDecimal.valueOf(100))
                .reviewsCount(0)
                .avgRating(BigDecimal.ZERO)
                .build();
        mentorRepository.save(mentor);
        mentorId = mentor.getId();
        mentorToken = loginAsExistingUser(mentorUser.getEmail());

        User studentUser = User.builder()
                .email("student@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .build();
        userRepository.save(studentUser);

        Student student = Student.builder()
                .user(studentUser)
                .firstName("Test")
                .lastName("Student")
                .build();
        studentRepository.save(student);
        studentToken = loginAsExistingUser("student@test.com");
    }

    private String loginAsExistingUser(String email) throws Exception {

        LoginRequest loginRequest = new LoginRequest(email, "password");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);

        return response.token();
    }

    @Test
    void bookLesson_shouldReturn201_whenRequestIsValid() throws Exception {

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                mentorId, "Topic", LocalDateTime.now().plusDays(1), 60);

        mockMvc.perform(post("/api/lessons")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLessonRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void bookLesson_shouldReturn409_whenTimeOverlaps() throws Exception {

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                mentorId, "Topic", LocalDateTime.now().plusDays(1), 60);

        mockMvc.perform(post("/api/lessons")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLessonRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/lessons")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLessonRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void bookLesson_shouldReturn404_whenMentorNotFound() throws Exception {

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                999L, "Topic", LocalDateTime.now().plusDays(1), 60);

        mockMvc.perform(post("/api/lessons")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLessonRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_shouldReturn201_whenLessonCompleted() throws Exception {

        CreateReviewRequest createReviewRequest = new CreateReviewRequest("Comment", 4);

        Long lessonId = createAndCompleteLesson();

        mockMvc.perform(post("/api/lessons/" + lessonId + "/review")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void createReview_shouldReturn409_whenReviewAlreadyExists() throws Exception {

        CreateReviewRequest createReviewRequest = new CreateReviewRequest("Comment", 4);

        Long lessonId = createAndCompleteLesson();

        mockMvc.perform(post("/api/lessons/" + lessonId + "/review")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/lessons/" + lessonId + "/review")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isConflict());
    }

    private Long createAndCompleteLesson() throws Exception {

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                mentorId, "Topic", LocalDateTime.now().plusDays(1), 60);

        MvcResult result = mockMvc.perform(post("/api/lessons")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createLessonRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LessonResponse lessonResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), LessonResponse.class);

        UpdateLessonStatusRequest updateLessonStatusRequest = new UpdateLessonStatusRequest(LessonStatus.COMPLETED);

        mockMvc.perform(patch("/api/lessons/" + lessonResponse.id() + "/status")
                        .header("Authorization", "Bearer " + mentorToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateLessonStatusRequest)))
                .andExpect(status().isOk());

        return lessonResponse.id();
    }
}
