package net.celloscope.aes.application.port.in;

import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import reactor.core.publisher.Mono;

public interface EncryptDataUseCase {

    Mono<CryptoOperationResult> encrypt(CryptoCommand command);
}
