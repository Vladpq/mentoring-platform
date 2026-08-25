package com.github.valdpq.mentoringplatform.lesson.event;

import com.github.valdpq.mentoringplatform.lesson.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonEventConsumer {

    private final LessonRepository lessonRepository;

    @KafkaListener(topics = "lesson-completed", groupId = "mentoring-platform")
    @Transactional
    public void handleLessonCompleted(LessonCompletedEvent event) {

        lessonRepository.markReviewReminderSent(event.lessonId());

        log.info("Lesson completed: lessonId={}, studentId={}, mentorId={}.",
                event.lessonId(), event.studentId(), event.mentorId());
    }
}
