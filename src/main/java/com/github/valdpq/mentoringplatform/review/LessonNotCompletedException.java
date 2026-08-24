package com.github.valdpq.mentoringplatform.review;

public class LessonNotCompletedException extends RuntimeException {
    public LessonNotCompletedException(String message) {
        super(message);
    }
}
