package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorNotFoundException;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MentorRepository mentorRepository;

    @InjectMocks
    private LessonService lessonService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void bookLesson_shouldThrowMentorNotFoundException_whenMentorDoesNotExists() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                999L, "", LocalDateTime.now(), 60);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User()));
        when(studentRepository.findByUserId(any()))
                .thenReturn(Optional.of(new Student()));
        when(mentorRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(MentorNotFoundException.class,
                () -> lessonService.bookLesson(createLessonRequest));
    }

    @Test
    public void bookLesson_shouldLessonOverlapException_whenLessonTimeConflict() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                999L, "", LocalDateTime.now(), 60);

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User()));
        when(studentRepository.findByUserId(any()))
                .thenReturn(Optional.of(new Student()));
        when(mentorRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.of(new Mentor()));
        when(lessonRepository.existsOverlappingLesson(any(), any(), any()))
                .thenReturn(true);

        assertThrows(LessonOverlapException.class,
                () -> lessonService.bookLesson(createLessonRequest));
    }

    @Test
    public void bookLesson_shouldReturnLessonResponse_whenLessonIsSaved() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateLessonRequest createLessonRequest = new CreateLessonRequest(
                999L, "topic", LocalDateTime.now(), 60);

        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Student student = Student.builder()
                .id(1L)
                .firstName("student")
                .lastName("name")
                .build();
        Mentor mentor = Mentor.builder()
                .id(999L)
                .firstName("mentor")
                .lastName("test")
                .build();

        when(userRepository.findByEmail("student@test.com"))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(mentorRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.of(mentor));
        when(lessonRepository.existsOverlappingLesson(any(), any(), any()))
                .thenReturn(false);

        LessonResponse response = lessonService.bookLesson(createLessonRequest);

        assertNotNull(response);
        assertEquals(60, response.durationMinutes());
        verify(lessonRepository).save(any(Lesson.class));
    }
}
