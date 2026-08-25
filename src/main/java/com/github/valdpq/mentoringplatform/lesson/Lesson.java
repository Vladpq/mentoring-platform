package com.github.valdpq.mentoringplatform.lesson;

import com.github.valdpq.mentoringplatform.mentor.Mentor;
import com.github.valdpq.mentoringplatform.student.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String topic;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private LessonStatus status = LessonStatus.SCHEDULED;

    @Builder.Default
    @Column(name = "review_reminder_sent", nullable = false)
    private Boolean reviewReminderSent = false;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void calculateEndTime() {
        this.endTime = this.startTime.plusMinutes(this.durationMinutes);
    }
}
