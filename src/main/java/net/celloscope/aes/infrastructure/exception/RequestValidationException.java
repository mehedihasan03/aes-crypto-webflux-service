package net.celloscope.aes.infrastructure.exception;

import java.util.List;

public class RequestValidationException extends RuntimeException {

    private final List<String> details;

    public RequestValidationException(List<String> details) {
        super("Request validation failed");
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
