package org.example.starterloghttp.exception;

public class AdviceException extends RuntimeException {
    public AdviceException(final String message, final Throwable e) {
        super(message);
    }
}
