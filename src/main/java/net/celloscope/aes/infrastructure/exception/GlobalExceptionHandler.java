package net.celloscope.aes.infrastructure.exception;

import java.time.Instant;
import java.util.List;
import net.celloscope.aes.adapter.in.web.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {
        List<String> details = exception.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", exchange, details);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnreadableJson(
            ServerWebInputException exception,
            ServerWebExchange exchange
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "Request body must be valid JSON", exchange, List.of());
    }

    @ExceptionHandler(InvalidCryptoInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCryptoInput(
            InvalidCryptoInputException exception,
            ServerWebExchange exchange
    ) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(CryptoOperationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCryptoOperation(
            CryptoOperationException exception,
            ServerWebExchange exchange
    ) {
        LOGGER.error("Crypto operation failed", exception);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Crypto operation failed", exchange, List.of());
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnexpected(Exception exception, ServerWebExchange exchange) {
        LOGGER.error("Unexpected request failure", exception);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", exchange, List.of());
    }

    private Mono<ResponseEntity<ErrorResponse>> buildError(
            HttpStatus status,
            String message,
            ServerWebExchange exchange,
            List<String> details
    ) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                details
        );

        return Mono.just(ResponseEntity.status(status).body(response));
    }
}
