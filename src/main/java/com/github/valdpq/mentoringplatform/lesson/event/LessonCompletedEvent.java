package com.github.valdpq.mentoringplatform.lesson.event;

public record LessonCompletedEvent(
        Long lessonId,
        Long mentorId,
        Long studentId
) {
}
