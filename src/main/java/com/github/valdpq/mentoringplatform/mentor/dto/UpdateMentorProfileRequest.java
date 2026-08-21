package com.github.valdpq.mentoringplatform.mentor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateMentorProfileRequest(

        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        String bio,

        @Size(max = 100, message = "Specialization must not exceed 100 characters")
        String specialization,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price per hour must be positive")
        @Digits(integer = 8, fraction = 2, message = "Price per hour must have at most 8 integer digits and 2 decimal places")
        BigDecimal pricePerHour
) {
}
