package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.auth.CurrentUserProvider;
import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.lesson.dto.UpdateLessonStatusRequest;
import com.github.valdpq.mentoringplatform.lesson.event.LessonCompletedEvent;
import com.github.valdpq.mentoringplatform.lesson.event.LessonEventProducer;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorNotFoundException;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentNotFoundException;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
import com.github.valdpq.mentoringplatform.user.Role;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final LessonEventProducer lessonEventProducer;

    @Transactional
    public LessonResponse bookLesson(CreateLessonRequest request) {

        String userEmail = CurrentUserProvider.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Mentor mentor = mentorRepository.findByIdForUpdate(request.mentorId())
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));

        if (lessonRepository.existsOverlappingLesson(
                mentor,
                request.startTime(),
                request.startTime().plusMinutes(request.durationMinutes()))) {
            throw new LessonOverlapException("Lesson time conflicts with an existing booking");
        }

        Lesson lesson = Lesson.builder()
                .mentor(mentor)
                .student(student)
                .topic(request.topic())
                .durationMinutes(request.durationMinutes())
                .startTime(request.startTime())
                .build();

        lessonRepository.save(lesson);

        return LessonResponse.fromEntity(lesson);
    }

    public Page<LessonResponse> getMyLessons(Pageable pageable) {

        String userEmail = CurrentUserProvider.getCurrentUserEmail();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == Role.MENTOR) {
            Mentor mentor = mentorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));
            return lessonRepository.findAllByMentor(mentor, pageable)
                    .map(LessonResponse::fromEntity);
        } else {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new StudentNotFoundException("Student not found"));
            return lessonRepository.findAllByStudent(student, pageable)
                    .map(LessonResponse::fromEntity);
        }
    }

    @Transactional
    public LessonResponse changeLessonStatus(Long lessonId, UpdateLessonStatusRequest request) {
        String userEmail = CurrentUserProvider.getCurrentUserEmail();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Mentor mentor = mentorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        if (!Objects.equals(lesson.getMentor().getId(), mentor.getId())) {
            throw new InvalidSessionOwnerException("Cannot change status of someone else's lesson");
        }

        if (lesson.getStatus() == LessonStatus.CANCELLED || lesson.getStatus() == LessonStatus.COMPLETED) {
            throw new IllegalLessonStatusTransitionException(
                    "Cannot change status of a " + lesson.getStatus() + " lesson");
        }

        lesson.setStatus(request.newStatus());
        lessonRepository.save(lesson);

        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            lessonEventProducer.publishLessonCompleted(
                    new LessonCompletedEvent(lesson.getId(), lesson.getMentor().getId(), lesson.getStudent().getId()));
        }

        return LessonResponse.fromEntity(lesson);
    }
}
