CREATE TABLE mentors
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    first_name     VARCHAR(255)  NOT NULL,
    last_name      VARCHAR(255)  NOT NULL,
    bio            TEXT,
    specialization VARCHAR(100),
    price_per_hour NUMERIC(10, 2),
    avg_rating     NUMERIC(3, 2) NOT NULL DEFAULT 0,
    reviews_count  INTEGER       NOT NULL DEFAULT 0,

    CONSTRAINT uq_mentors_user_id UNIQUE (user_id),
    CONSTRAINT fk_mentors_users FOREIGN KEY (user_id) REFERENCES users
);

CREATE TABLE students
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,

    CONSTRAINT uq_students_user_id UNIQUE (user_id),
    CONSTRAINT fk_students_users FOREIGN KEY (user_id) REFERENCES users
)