package xyz.nowiknowmy.hogwarts.exceptions;

public class UnexpectedAccessException extends RuntimeException {
    public UnexpectedAccessException(String message) {
        super(message);
    }
}
