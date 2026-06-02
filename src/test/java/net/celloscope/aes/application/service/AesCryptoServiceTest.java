package net.celloscope.aes.application.service;

import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.application.port.out.CryptoProvider;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class AesCryptoServiceTest {

    @Test
    void encryptDelegatesToProviderAndReturnsReactiveResult() {
        CryptoProvider provider = new StubCryptoProvider();
        AesCryptoService service = new AesCryptoService(provider);

        StepVerifier.create(service.encrypt(new CryptoCommand("plain", "0123456789abcdef", null)))
                .expectNext(new CryptoOperationResult("ciphertext", "AES/GCM/NoPadding", "generated-nonce", 128))
                .verifyComplete();
    }

    @Test
    void decryptDelegatesToProviderAndReturnsReactiveResult() {
        CryptoProvider provider = new StubCryptoProvider();
        AesCryptoService service = new AesCryptoService(provider);

        StepVerifier.create(service.decrypt(new CryptoCommand("ciphertext", "0123456789abcdef", "nonce")))
                .expectNext(new CryptoOperationResult("plaintext", "AES/GCM/NoPadding", "nonce", 128))
                .verifyComplete();
    }

    private static class StubCryptoProvider implements CryptoProvider {

        @Override
        public String algorithm() {
            return "AES/GCM/NoPadding";
        }

        @Override
        public CryptoOperationResult encrypt(CryptoCommand command) {
            return new CryptoOperationResult("ciphertext", algorithm(), "generated-nonce", 128);
        }

        @Override
        public CryptoOperationResult decrypt(CryptoCommand command) {
            return new CryptoOperationResult("plaintext", algorithm(), command.iv(), 128);
        }
    }
}
