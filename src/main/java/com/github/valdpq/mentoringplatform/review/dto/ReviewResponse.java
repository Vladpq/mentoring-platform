package com.github.valdpq.mentoringplatform.review.dto;

import com.github.valdpq.mentoringplatform.review.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long lessonId,
        String studentName,
        String comment,
        Integer rating,
        LocalDateTime createdAt
) {

    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getLesson().getId(),
                review.getLesson().getStudent().getFirstName() + " " +
                        review.getLesson().getStudent().getLastName(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt()
        );
    }
}
