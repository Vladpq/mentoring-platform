package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.auth.CurrentUserProvider;
import com.github.valdpq.mentoringplatform.lesson.dto.CreateLessonRequest;
import com.github.valdpq.mentoringplatform.lesson.dto.LessonResponse;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorNotFoundException;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
import com.github.valdpq.mentoringplatform.user.User;
import com.github.valdpq.mentoringplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public LessonResponse bookLesson(CreateLessonRequest request) {

        String userEmail = CurrentUserProvider.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Student profile not found for user " + currentUser.getId()));

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
}
