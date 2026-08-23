package com.github.valdpq.mentoringplatform.lesson.event;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonEventProducerTest {

    @Mock
    private KafkaTemplate<String, LessonCompletedEvent> kafkaTemplate;

    @InjectMocks
    private LessonEventProducer producer;

    @Test
    public void publishLessonCompleted_shouldSendLessonCompletedEvent_whenKafkaTemplateSend() {

        LessonCompletedEvent event = new LessonCompletedEvent(1L, 1L, 1L);

        SendResult<String, LessonCompletedEvent> result = mock(SendResult.class);
        RecordMetadata metadata = mock(RecordMetadata.class);

        when(result.getRecordMetadata())
                .thenReturn(metadata);
        when(metadata.offset())
                .thenReturn(0L);
        when(kafkaTemplate.send(anyString(),any(LessonCompletedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(result));

        producer.publishLessonCompleted(event);

        verify(kafkaTemplate).send("lesson-completed", event);
    }

    @Test
    public void publishLessonCompleted_shouldLogError_whenKafkaSendFails() {

        LessonCompletedEvent event = new LessonCompletedEvent(1L, 1L, 1L);

        CompletableFuture<SendResult<String, LessonCompletedEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(kafkaTemplate.send(anyString(),any(LessonCompletedEvent.class)))
                .thenReturn(failedFuture);

        producer.publishLessonCompleted(event);

        verify(kafkaTemplate).send("lesson-completed", event);
    }
}
