package com.github.valdpq.mentoringplatform.lesson;

public class IllegalLessonStatusTransitionException extends RuntimeException {
    public IllegalLessonStatusTransitionException(String message) {
        super(message);
    }
}
