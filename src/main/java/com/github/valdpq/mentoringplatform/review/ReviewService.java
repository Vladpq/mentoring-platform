package com.github.valdpq.mentoringplatform.review;

import com.github.valdpq.mentoringplatform.auth.CurrentUserProvider;
import com.github.valdpq.mentoringplatform.lesson.*;
import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.mentor.MentorNotFoundException;
import com.github.valdpq.mentoringplatform.mentor.MentorRepository;
import com.github.valdpq.mentoringplatform.review.dto.CreateReviewRequest;
import com.github.valdpq.mentoringplatform.review.dto.ReviewResponse;
import com.github.valdpq.mentoringplatform.student.Student;
import com.github.valdpq.mentoringplatform.student.StudentNotFoundException;
import com.github.valdpq.mentoringplatform.student.StudentRepository;
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
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public ReviewResponse createReview(Long lessonId, CreateReviewRequest request) {

        String userEmail = CurrentUserProvider.getCurrentUserEmail();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found"));

        if (!Objects.equals(lesson.getStudent().getId(), student.getId())) {
            throw new InvalidSessionOwnerException("Cannot create review for someone else's lesson");
        }

        if (lesson.getStatus() != LessonStatus.COMPLETED) {
            throw new LessonNotCompletedException("Lesson should be completed");
        }

        if (reviewRepository.existsReviewByLesson(lesson)) {
            throw new ReviewAlreadyExistsException("Review for lesson " + lessonId + " already exists");
        }

        Review review = Review.builder()
                .lesson(lesson)
                .comment(request.comment())
                .rating(request.rating())
                .build();

        reviewRepository.save(review);

        Mentor mentor = mentorRepository.findByIdForUpdate(lesson.getMentor().getId())
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));

        mentor.setReviewsCount((int) reviewRepository.countReviewByMentor(mentor));
        mentor.setAvgRating(reviewRepository.calculateAvgRating(mentor));
        mentorRepository.save(mentor);

        return ReviewResponse.fromEntity(review);
    }

    public Page<ReviewResponse> getMentorReviews(Long mentorId, Pageable pageable) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found"));

        return reviewRepository.getAllByLessonMentor(mentor, pageable)
                .map(ReviewResponse::fromEntity);
    }
}
