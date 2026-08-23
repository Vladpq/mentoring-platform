package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.lesson.dto.UpdateLessonStatusRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    @Test
    public void getMyLessons_shouldReturnMentorLessons_whenUserIsMentor() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();
        Mentor mentor = Mentor.builder()
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.SCHEDULED)
                .build();

        when(userRepository.findByEmail("mentor@test.com"))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(1L))
                .thenReturn(Optional.of(mentor));
        when(lessonRepository.findAllByMentor(mentor, pageable))
                .thenReturn(new PageImpl<>(List.of(lesson)));

        Page<LessonResponse> result = lessonService.getMyLessons(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(lessonRepository, never()).findAllByStudent(any(), any());
    }

    @Test
    public void getMyLessons_shouldReturnStudentLessons_whenUserIsStudent() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder()
                .id(1L)
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();
        Mentor mentor = Mentor.builder()
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.SCHEDULED)
                .build();

        when(userRepository.findByEmail("student@test.com"))
                .thenReturn(Optional.of(user));
        when(studentRepository.findByUserId(1L))
                .thenReturn(Optional.of(student));
        when(lessonRepository.findAllByStudent(student, pageable))
                .thenReturn(new PageImpl<>(List.of(lesson)));

        Page<LessonResponse> result = lessonService.getMyLessons(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(lessonRepository, never()).findAllByMentor(any(), any());
    }

    @Test
    public void changeLessonStatus_shouldThrowInvalidSessionOwnerException_whenWrongOwner() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();
        Mentor owner = Mentor.builder()
                .id(2L)
                .firstName("first")
                .lastName("last")
                .build();
        Student student = Student.builder()
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(owner)
                .student(student)
                .status(LessonStatus.SCHEDULED)
                .build();

        UpdateLessonStatusRequest request = new UpdateLessonStatusRequest(LessonStatus.CANCELLED);

        when(userRepository.findByEmail("mentor@test.com"))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(1L))
                .thenReturn(Optional.of(mentor));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        assertThrows(InvalidSessionOwnerException.class,
                () -> lessonService.changeLessonStatus(1L, request));
    }

    @Test
    public void changeLessonStatus_shouldIllegalLessonStatusTransitionException_whenStatusIsLocked() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();

        Student student = Student.builder()
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.COMPLETED)
                .build();

        UpdateLessonStatusRequest request = new UpdateLessonStatusRequest(LessonStatus.CANCELLED);

        when(userRepository.findByEmail("mentor@test.com"))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(1L))
                .thenReturn(Optional.of(mentor));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        assertThrows(IllegalLessonStatusTransitionException.class,
                () -> lessonService.changeLessonStatus(1L, request));
    }

    @Test
    public void changeLessonStatus_shouldReturnLessonWithNewStatus_whenStatusIsChanged() {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "mentor@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .role(Role.MENTOR)
                .build();
        Mentor mentor = Mentor.builder()
                .id(1L)
                .user(user)
                .firstName("first")
                .lastName("last")
                .build();

        Student student = Student.builder()
                .firstName("first")
                .lastName("last")
                .build();
        Lesson lesson = Lesson.builder()
                .id(1L)
                .mentor(mentor)
                .student(student)
                .status(LessonStatus.SCHEDULED)
                .build();

        UpdateLessonStatusRequest request = new UpdateLessonStatusRequest(LessonStatus.CANCELLED);

        when(userRepository.findByEmail("mentor@test.com"))
                .thenReturn(Optional.of(user));
        when(mentorRepository.findByUserId(1L))
                .thenReturn(Optional.of(mentor));
        when(lessonRepository.findById(1L))
                .thenReturn(Optional.of(lesson));

        LessonResponse response = lessonService.changeLessonStatus(1L, request);

        assertNotNull(response);
        assertEquals(LessonStatus.CANCELLED, response.status());
        verify(lessonRepository).save(any(Lesson.class));
    }
}
