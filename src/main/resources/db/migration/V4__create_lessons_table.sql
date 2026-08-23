CREATE TABLE lessons
(
    id               BIGSERIAL PRIMARY KEY,
    mentor_id        BIGINT      NOT NULL,
    student_id       BIGINT      NOT NULL,
    topic            TEXT        NOT NULL,
    start_time       TIMESTAMP   NOT NULL,
    end_time         TIMESTAMP   NOT NULL,
    duration_minutes INT         NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),

    CONSTRAINT fk_lessons_mentors FOREIGN KEY (mentor_id) REFERENCES mentors,
    CONSTRAINT fk_lessons_students FOREIGN KEY (student_id) REFERENCES students,
    CONSTRAINT chk_lessons_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_lessons_duration_positive CHECK (duration_minutes > 0),
    CONSTRAINT chk_lessons_time_order CHECK (end_time > start_time)
);

CREATE INDEX idx_lessons_mentor_id_start_time ON lessons (mentor_id, start_time);
CREATE INDEX idx_lessons_student_id ON lessons (student_id);