package net.celloscope.aes.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crypto.aes")
public record AesCryptoProperties(
        int nonceLengthBytes,
        int tagLengthBits
) {

    public AesCryptoProperties {
        if (nonceLengthBytes != 12) {
            throw new IllegalArgumentException("AES-GCM nonce length must be 12 bytes");
        }

        if (tagLengthBits != 128 && tagLengthBits != 120 && tagLengthBits != 112
                && tagLengthBits != 104 && tagLengthBits != 96) {
            throw new IllegalArgumentException("AES-GCM tag length must be one of 96, 104, 112, 120, or 128 bits");
        }
    }
}
