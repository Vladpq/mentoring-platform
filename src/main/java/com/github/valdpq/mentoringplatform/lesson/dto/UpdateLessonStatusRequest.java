package com.github.valdpq.mentoringplatform.lesson.dto;

import com.github.valdpq.mentoringplatform.lesson.LessonStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLessonStatusRequest(
        @NotNull(message = "New status id required")
        LessonStatus newStatus
) {
}
