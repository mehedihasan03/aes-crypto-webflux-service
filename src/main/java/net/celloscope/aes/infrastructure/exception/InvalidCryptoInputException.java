package net.celloscope.aes.infrastructure.exception;

public class InvalidCryptoInputException extends RuntimeException {

    public InvalidCryptoInputException(String message) {
        super(message);
    }

    public InvalidCryptoInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
