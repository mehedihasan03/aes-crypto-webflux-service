package net.celloscope.aes.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import net.celloscope.aes.adapter.in.web.dto.request.CryptoRequest;
import net.celloscope.aes.adapter.in.web.dto.response.CryptoMetadata;
import net.celloscope.aes.adapter.in.web.dto.response.CryptoResponse;
import net.celloscope.aes.adapter.in.web.dto.response.ErrorResponse;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.infrastructure.exception.RequestValidationException;
import net.celloscope.aes.infrastructure.exception.UnsupportedContentTypeException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class HelperUtil {

    private HelperUtil() {
    }

    public static <T> Mono<T> requireBody(Mono<T> body) {
        return body.switchIfEmpty(Mono.error(
                new RequestValidationException(List.of("body: request body is required"))
        ));
    }

    public static Mono<Void> requireJsonContentType(ServerRequest request) {
        return request.headers()
                .contentType()
                .filter(MediaType.APPLICATION_JSON::isCompatibleWith)
                .map(contentType -> Mono.<Void>empty())
                .orElseGet(() -> Mono.error(new UnsupportedContentTypeException(
                        "Content-Type must be application/json"
                )));
    }

    public static <T> Mono<T> validate(Validator validator, T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return Mono.just(request);
        }

        List<String> details = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .toList();

        return Mono.error(new RequestValidationException(details));
    }

    public static <T extends CryptoRequest> CryptoCommand toCommand(T request) {
        return new CryptoCommand(request.data(), request.secretKey(), request.iv());
    }

    public static Mono<ServerResponse> okResponse(CryptoOperationResult result) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(toResponse(result));
    }

    public static CryptoResponse toResponse(CryptoOperationResult result) {
        return new CryptoResponse(
                result.result(),
                result.algorithm(),
                new CryptoMetadata(result.iv(), result.tagLengthBits())
        );
    }

    public static Mono<ServerResponse> errorResponse(
            HttpStatus status,
            String message,
            ServerRequest request,
            List<String> details
    ) {
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.path(),
                details
        );

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }

    public static void logRequestStarted(Logger logger, ServerRequest request, String operation, String requestId) {
        logger.info(
                "Crypto request received requestId={} operation={} method={} path={} contentType={}",
                requestId,
                operation,
                request.method().name(),
                request.path(),
                request.headers().contentType().map(MediaType::toString).orElse("unknown")
        );
    }

    public static void logRequestBody(Logger logger, String operation, String requestId, CryptoRequest request) {
        logger.info(
                "Crypto request body requestId={} operation={} dataLength={} secretKeyLength={} ivPresent={} ivLength={}",
                requestId,
                operation,
                length(request.data()),
                length(request.secretKey()),
                hasText(request.iv()),
                length(request.iv())
        );
    }

    public static void logResponse(Logger logger, String operation, String requestId, CryptoOperationResult result) {
        logger.info(
                "Crypto response prepared requestId={} operation={} algorithm={} resultLength={} ivPresent={} ivLength={} tagLengthBits={}",
                requestId,
                operation,
                result.algorithm(),
                length(result.result()),
                hasText(result.iv()),
                length(result.iv()),
                result.tagLengthBits()
        );
    }

    public static void logClientError(
            Logger logger,
            ServerRequest request,
            HttpStatus status,
            String message,
            List<String> details
    ) {
        logger.warn(
                "Crypto request failed requestId={} method={} path={} status={} message={} details={}",
                requestId(request),
                request.method().name(),
                request.path(),
                status.value(),
                message,
                details
        );
    }

    public static void logServerError(
            Logger logger,
            ServerRequest request,
            HttpStatus status,
            String message,
            Throwable error
    ) {
        logger.error(
                "Crypto request failed requestId={} method={} path={} status={} message={}",
                requestId(request),
                request.method().name(),
                request.path(),
                status.value(),
                message,
                error
        );
    }

    public static String requestId(ServerRequest request) {
        String traceId = request.headers().firstHeader("Trace-Id");
        if (hasText(traceId)) {
            return traceId;
        }

        String requestId = request.headers().firstHeader("Request-Id");
        if (hasText(requestId)) {
            return requestId;
        }

        return request.exchange().getRequest().getId();
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static int length(String value) {
        return value == null ? 0 : value.length();
    }
}
