package com.github.valdpq.mentoringplatform.lesson.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonEventProducer {

    private final KafkaTemplate<String, LessonCompletedEvent> kafkaTemplate;

    public void publishLessonCompleted(LessonCompletedEvent event) {
        kafkaTemplate.send("lesson-completed", event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish LessonCompletedEvent for lessonId={}", event.lessonId(), ex);
            } else {
                log.info("Successfully publish LessonCompletedEvent for lessonId={}, offset={}",
                        event.lessonId(), result.getRecordMetadata().offset());
            }
        });
    }
}
