package com.github.valdpq.mentoringplatform.lesson.event;

import com.github.valdpq.mentoringplatform.lesson.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LessonEventConsumerTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonEventConsumer lessonEventConsumer;

    @Test
    public void handleLessonCompleted_shouldMarkReviewReminderSent_whenEventIsSent() {

        LessonCompletedEvent event = new LessonCompletedEvent(1L, 1L, 1L);

        lessonEventConsumer.handleLessonCompleted(event);

        verify(lessonRepository).markReviewReminderSent(1L);
    }
}
