package net.celloscope.aes.adapter.in.web.handler;

import jakarta.validation.Validator;
import java.util.function.Function;
import net.celloscope.aes.adapter.in.web.dto.request.CryptoRequest;
import net.celloscope.aes.adapter.in.web.dto.request.DecryptRequest;
import net.celloscope.aes.adapter.in.web.dto.request.EncryptRequest;
import net.celloscope.aes.application.port.in.DecryptDataUseCase;
import net.celloscope.aes.application.port.in.EncryptDataUseCase;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.infrastructure.exception.ErrorResponseMapper;
import net.celloscope.aes.util.HelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class CryptoHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoHandler.class);

    private final EncryptDataUseCase encryptDataUseCase;
    private final DecryptDataUseCase decryptDataUseCase;
    private final ErrorResponseMapper errorResponseMapper;
    private final Validator validator;

    @Autowired
    public CryptoHandler(
            EncryptDataUseCase encryptDataUseCase,
            DecryptDataUseCase decryptDataUseCase,
            ErrorResponseMapper errorResponseMapper,
            Validator validator
    ) {
        this.encryptDataUseCase = encryptDataUseCase;
        this.decryptDataUseCase = decryptDataUseCase;
        this.errorResponseMapper = errorResponseMapper;
        this.validator = validator;
    }

    public Mono<ServerResponse> encrypt(ServerRequest request) {
        return handle(request, EncryptRequest.class, "encrypt", encryptDataUseCase::encrypt);
    }

    public Mono<ServerResponse> decrypt(ServerRequest request) {
        return handle(request, DecryptRequest.class, "decrypt", decryptDataUseCase::decrypt);
    }

    private <T extends CryptoRequest> Mono<ServerResponse> handle(
            ServerRequest request,
            Class<T> requestType,
            String operation,
            Function<CryptoCommand, Mono<CryptoOperationResult>> useCase
    ) {
        String requestId = HelperUtil.requestId(request);
        HelperUtil.logRequestStarted(LOGGER, request, operation, requestId);

        return HelperUtil.requireJsonContentType(request)
                .then(request.bodyToMono(requestType))
                .transform(HelperUtil::requireBody)
                .doOnNext(body -> HelperUtil.logRequestBody(LOGGER, operation, requestId, body))
                .flatMap(body -> HelperUtil.validate(validator, body))
                .map(HelperUtil::toCommand)
                .flatMap(useCase)
                .doOnNext(result -> HelperUtil.logResponse(LOGGER, operation, requestId, result))
                .flatMap(HelperUtil::okResponse)
                .onErrorResume(error -> errorResponseMapper.toServerResponse(error, request));
    }
}
