package net.celloscope.aes.adapter.in.web.handler;

import jakarta.validation.Valid;
import net.celloscope.aes.adapter.in.web.dto.request.DecryptRequest;
import net.celloscope.aes.adapter.in.web.dto.request.EncryptRequest;
import net.celloscope.aes.adapter.in.web.dto.response.CryptoMetadata;
import net.celloscope.aes.adapter.in.web.dto.response.CryptoResponse;
import net.celloscope.aes.application.port.in.DecryptDataUseCase;
import net.celloscope.aes.application.port.in.EncryptDataUseCase;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/crypto/aes")
public class CryptoController {

    private final EncryptDataUseCase encryptDataUseCase;
    private final DecryptDataUseCase decryptDataUseCase;

    @Autowired
    public CryptoController(EncryptDataUseCase encryptDataUseCase, DecryptDataUseCase decryptDataUseCase) {
        this.encryptDataUseCase = encryptDataUseCase;
        this.decryptDataUseCase = decryptDataUseCase;
    }

    @PostMapping("/encrypt")
    public Mono<ResponseEntity<CryptoResponse>> encrypt(@Valid @RequestBody EncryptRequest request) {
        return encryptDataUseCase.encrypt(toCommand(request))
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/decrypt")
    public Mono<ResponseEntity<CryptoResponse>> decrypt(@Valid @RequestBody DecryptRequest request) {
        return decryptDataUseCase.decrypt(toCommand(request))
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    private CryptoCommand toCommand(EncryptRequest request) {
        return new CryptoCommand(request.data(), request.secretKey(), request.iv());
    }

    private CryptoCommand toCommand(DecryptRequest request) {
        return new CryptoCommand(request.data(), request.secretKey(), request.iv());
    }

    private CryptoResponse toResponse(CryptoOperationResult result) {
        return new CryptoResponse(
                result.result(),
                result.algorithm(),
                new CryptoMetadata(result.iv(), result.tagLengthBits())
        );
    }
}
