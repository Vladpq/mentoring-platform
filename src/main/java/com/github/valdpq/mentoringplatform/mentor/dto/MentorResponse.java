package com.github.valdpq.mentoringplatform.mentor.dto;

import java.math.BigDecimal;

public record MentorResponse(
        Long id,
        String firstName,
        String lastName,
        String bio,
        String specialization,
        BigDecimal pricePerHour,
        BigDecimal avgRating,
        Integer reviewsCount
) {
}
