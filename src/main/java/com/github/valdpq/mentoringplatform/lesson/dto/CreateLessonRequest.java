package com.github.valdpq.mentoringplatform.lesson.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateLessonRequest(
        @NotNull(message = "Mentor id is required")
        Long mentorId,

        @NotBlank(message = "Topic is required")
        String topic,

        @NotNull(message = "Start time is required")
        @Future(message = "Lesson date must be a future date")
        LocalDateTime startTime,

        @NotNull(message = "Duration is required")
        @Min(value = 15, message = "Duration must be longer than 15 minutes")
        @Max(value = 180, message = "Duration must be shorter than 180 minutes")
        Integer durationMinutes
) {
}
