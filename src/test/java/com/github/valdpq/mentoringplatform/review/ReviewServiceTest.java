package com.github.valdpq.mentoringplatform.review;

import com.github.valdpq.mentoringplatform.lesson.*;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.review.dto.CreateReviewRequest;
import com.github.valdpq.mentoringplatform.review.dto.ReviewResponse;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private MentorRepository mentorRepository;

    @InjectMocks
    private ReviewService reviewService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void createReview_shouldThrowLessonNotFoundException_whenLessonDoesNotExists() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateReviewRequest request = new CreateReviewRequest("comment", 5);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User()));
        when(studentRepository.findByUserId(any()))
                .thenReturn(Optional.of(new Student()));
        when(lessonRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(LessonNotFoundException.class,
                () -> reviewService.createReview(999L, request));
    }

    @Test
    public void createReview_shouldThrowInvalidSessionOwnerException_whenWrongOwner() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Student owner = Student.builder()
                .id(2L)
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(owner)
                .status(LessonStatus.COMPLETED)
                .build();

        CreateReviewRequest request = new CreateReviewRequest("comment", 4);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        assertThrows(InvalidSessionOwnerException.class, () -> reviewService.createReview(1L, request));
    }

    @Test
    public void createReview_shouldThrowLessonNotCompletedException_whenLessonNotCompleted() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.SCHEDULED)
                .build();

        CreateReviewRequest request = new CreateReviewRequest("comment", 4);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        assertThrows(LessonNotCompletedException.class, () -> reviewService.createReview(1L, request));
    }

    @Test
    public void createReview_shouldThrowReviewAlreadyExistsException_whenReviewAlreadyExists() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.COMPLETED)
                .build();

        CreateReviewRequest request = new CreateReviewRequest("comment", 4);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));
        when(reviewRepository.existsReviewByLesson(any()))
                .thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.createReview(1L, request));
    }

    @Test
    public void createReview_shouldReturnReviewResponseAndUpdateMentorRating_whenReviewIsCreated() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .firstName("first")
                .lastName("last")
                .avgRating(BigDecimal.valueOf(5))
                .build();
        Student student = Student.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.COMPLETED)
                .build();

        CreateReviewRequest request = new CreateReviewRequest("comment", 3);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));
        when(reviewRepository.existsReviewByLesson(any()))
                .thenReturn(false);
        when(mentorRepository.findByIdForUpdate(lesson.getMentor().getId()))
                .thenReturn(Optional.of(mentor));
        when(reviewRepository.countReviewByMentor(mentor))
                .thenReturn(3L);
        when(reviewRepository.calculateAvgRating(mentor))
                .thenReturn(BigDecimal.valueOf(4.00));

        ReviewResponse response = reviewService.createReview(1L, request);

        assertNotNull(response);
        verify(reviewRepository).save(any(Review.class));
        verify(mentorRepository).save(any(Mentor.class));
        verify(mentorRepository).findByIdForUpdate(anyLong());
        assertEquals(0, BigDecimal.valueOf(4.00).compareTo(mentor.getAvgRating()));
    }
}
