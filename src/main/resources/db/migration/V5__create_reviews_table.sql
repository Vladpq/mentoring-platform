CREATE TABLE reviews
(
    id         BIGSERIAL PRIMARY KEY,
    lesson_id  BIGINT    NOT NULL,
    comment    TEXT,
    rating     INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_reviews_lessons FOREIGN KEY (lesson_id) REFERENCES lessons,
    CONSTRAINT uq_reviews_lesson_id UNIQUE (lesson_id),
    CONSTRAINT chk_reviews_rating CHECK (rating IN (1, 2, 3, 4, 5))
)