package com.github.valdpq.mentoringplatform.lesson.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic lessonCompletedTopic() {
        return TopicBuilder
                .name("lesson-completed")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
