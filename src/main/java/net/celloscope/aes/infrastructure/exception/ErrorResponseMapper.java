package net.celloscope.aes.infrastructure.exception;

import java.util.List;
import net.celloscope.aes.util.HelperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.CodecException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
public class ErrorResponseMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorResponseMapper.class);

    public Mono<ServerResponse> toServerResponse(Throwable error, ServerRequest request) {
        if (error instanceof RequestValidationException exception) {
            HelperUtil.logClientError(LOGGER, request, HttpStatus.BAD_REQUEST, "Request validation failed", exception.details());
            return HelperUtil.errorResponse(HttpStatus.BAD_REQUEST, "Request validation failed", request, exception.details());
        }

        if (error instanceof UnsupportedContentTypeException exception) {
            HelperUtil.logClientError(LOGGER, request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), List.of());
            return HelperUtil.errorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request, List.of());
        }

        if (error instanceof InvalidCryptoInputException exception) {
            HelperUtil.logClientError(LOGGER, request, HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
            return HelperUtil.errorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
        }

        if (error instanceof ServerWebInputException || error instanceof CodecException) {
            HelperUtil.logClientError(LOGGER, request, HttpStatus.BAD_REQUEST, "Request body must be valid JSON", List.of());
            return HelperUtil.errorResponse(HttpStatus.BAD_REQUEST, "Request body must be valid JSON", request, List.of());
        }

        if (error instanceof CryptoOperationException) {
            HelperUtil.logServerError(LOGGER, request, HttpStatus.INTERNAL_SERVER_ERROR, "Crypto operation failed", error);
            return HelperUtil.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Crypto operation failed", request, List.of());
        }

        HelperUtil.logServerError(LOGGER, request, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", error);
        return HelperUtil.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, List.of());
    }
}
