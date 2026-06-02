package net.celloscope.aes.infrastructure.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import net.celloscope.aes.adapter.out.crypto.AesGcmCryptoProvider;
import net.celloscope.aes.infrastructure.exception.InvalidCryptoInputException;
import net.celloscope.aes.domain.model.CryptoCommand;
import net.celloscope.aes.domain.model.CryptoOperationResult;
import net.celloscope.aes.infrastructure.config.AesCryptoProperties;
import org.junit.jupiter.api.Test;

class AesGcmCryptoProviderTest {

    private static final String AES_256_KEY = "0123456789abcdef0123456789abcdef";

    private final AesGcmCryptoProvider provider = new AesGcmCryptoProvider(
            new AesCryptoProperties(12, 128),
            new SecureRandom()
    );

    @Test
    void encryptAndDecryptRoundTripWithGeneratedNonce() {
        CryptoOperationResult encrypted = provider.encrypt(new CryptoCommand("Hello AES-GCM", AES_256_KEY, null));

        assertThat(encrypted.result()).isNotBlank();
        assertThat(encrypted.algorithm()).isEqualTo("AES/GCM/NoPadding");
        assertThat(Base64.getDecoder().decode(encrypted.iv())).hasSize(12);

        CryptoOperationResult decrypted = provider.decrypt(
                new CryptoCommand(encrypted.result(), AES_256_KEY, encrypted.iv())
        );

        assertThat(decrypted.result()).isEqualTo("Hello AES-GCM");
        assertThat(decrypted.iv()).isEqualTo(encrypted.iv());
    }

    @Test
    void encryptUsesProvidedBase64NonceWhenPresent() {
        String iv = Base64.getEncoder().encodeToString("123456789012".getBytes(StandardCharsets.UTF_8));

        CryptoOperationResult encrypted = provider.encrypt(new CryptoCommand("controlled nonce", AES_256_KEY, iv));

        assertThat(encrypted.iv()).isEqualTo(iv);
    }

    @Test
    void encryptionRejectsInvalidAesKeyLength() {
        assertThatThrownBy(() -> provider.encrypt(new CryptoCommand("data", "short-key", null)))
                .isInstanceOf(InvalidCryptoInputException.class)
                .hasMessageContaining("secretKey must be 16, 24, or 32 UTF-8 bytes");
    }

    @Test
    void decryptionRejectsTamperedCiphertext() {
        CryptoOperationResult encrypted = provider.encrypt(new CryptoCommand("integrity checked", AES_256_KEY, null));
        byte[] tamperedBytes = Base64.getDecoder().decode(encrypted.result());
        tamperedBytes[0] = (byte) (tamperedBytes[0] ^ 1);
        String tamperedCiphertext = Base64.getEncoder().encodeToString(tamperedBytes);

        assertThatThrownBy(() -> provider.decrypt(new CryptoCommand(tamperedCiphertext, AES_256_KEY, encrypted.iv())))
                .isInstanceOf(InvalidCryptoInputException.class)
                .hasMessageContaining("Unable to decrypt data");
    }

    @Test
    void decryptionRequiresNonce() {
        assertThatThrownBy(() -> provider.decrypt(new CryptoCommand("ciphertext", AES_256_KEY, null)))
                .isInstanceOf(InvalidCryptoInputException.class)
                .hasMessageContaining("iv is required");
    }
}
