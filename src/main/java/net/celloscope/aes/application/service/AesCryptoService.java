package net.celloscope.aes.application.service;

import net.celloscope.aes.application.port.in.DecryptDataUseCase;
import net.celloscope.aes.application.port.in.EncryptDataUseCase;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.application.port.out.CryptoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AesCryptoService implements EncryptDataUseCase, DecryptDataUseCase {

    private final CryptoProvider cryptoProvider;

    @Autowired
    public AesCryptoService(CryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }

    @Override
    public Mono<CryptoOperationResult> encrypt(CryptoCommand command) {
        return Mono.fromSupplier(() -> cryptoProvider.encrypt(command));
    }

    @Override
    public Mono<CryptoOperationResult> decrypt(CryptoCommand command) {
        return Mono.fromSupplier(() -> cryptoProvider.decrypt(command));
    }
}
