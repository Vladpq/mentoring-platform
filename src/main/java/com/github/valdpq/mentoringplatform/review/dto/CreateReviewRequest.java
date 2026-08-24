package com.github.valdpq.mentoringplatform.review.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record CreateReviewRequest(

        String comment,

        @NotNull(message = "Rating is required")
        @Range(min = 1, max = 5, message = "Rating must be between 1 and 5")
        Integer rating
) {
}
