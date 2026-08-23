package com.github.valdpq.mentoringplatform.lesson;

public class InvalidSessionOwnerException extends RuntimeException {
    public InvalidSessionOwnerException(String message) {
        super(message);
    }
}
