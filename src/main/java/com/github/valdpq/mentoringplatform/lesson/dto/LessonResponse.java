package com.github.valdpq.mentoringplatform.lesson.dto;

import com.github.valdpq.mentoringplatform.lesson.Lesson;
import com.github.valdpq.mentoringplatform.lesson.LessonStatus;

import java.time.LocalDateTime;

public record LessonResponse(
        Long id,
        String mentorName,
        String studentName,
        String topic,
        LocalDateTime startTime,
        Integer durationMinutes,
        LessonStatus status
) {
    public static LessonResponse fromEntity(Lesson lesson) {
        return new LessonResponse(lesson.getId(),
                lesson.getMentor().getFirstName() + " " + lesson.getMentor().getLastName(),
                lesson.getStudent().getFirstName() + " " + lesson.getStudent().getLastName(),
                lesson.getTopic(),
                lesson.getStartTime(),
                lesson.getDurationMinutes(),
                lesson.getStatus());
    }
}
