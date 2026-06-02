package net.celloscope.aes.infrastructure.exception;

public class CryptoOperationException extends RuntimeException {

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
