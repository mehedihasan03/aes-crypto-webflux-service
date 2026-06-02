package net.celloscope.aes.application.service;

import net.celloscope.aes.application.port.in.DecryptDataUseCase;
import net.celloscope.aes.application.port.in.EncryptDataUseCase;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.application.port.out.CryptoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
public class AesCryptoService implements EncryptDataUseCase, DecryptDataUseCase {

    private final CryptoProvider cryptoProvider;
    private final Scheduler cryptoScheduler;

    @Autowired
    public AesCryptoService(CryptoProvider cryptoProvider, @Qualifier("cryptoScheduler") Scheduler cryptoScheduler) {
        this.cryptoProvider = cryptoProvider;
        this.cryptoScheduler = cryptoScheduler;
    }

    @Override
    public Mono<CryptoOperationResult> encrypt(CryptoCommand command) {
        return Mono.fromCallable(() -> cryptoProvider.encrypt(command))
                .subscribeOn(cryptoScheduler);
    }

    @Override
    public Mono<CryptoOperationResult> decrypt(CryptoCommand command) {
        return Mono.fromCallable(() -> cryptoProvider.decrypt(command))
                .subscribeOn(cryptoScheduler);
    }
}
