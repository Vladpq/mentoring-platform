package com.github.valdpq.mentoringplatform.lesson.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LessonEventConsumer {

    @KafkaListener(topics = "lesson-completed", groupId = "mentoring-platform")
    public void handleLessonCompleted(LessonCompletedEvent event) {
        log.info("Lesson completed: lessonId={}, studentId={}, mentorId={}.",
                event.lessonId(), event.studentId(), event.mentorId());
    }
}
