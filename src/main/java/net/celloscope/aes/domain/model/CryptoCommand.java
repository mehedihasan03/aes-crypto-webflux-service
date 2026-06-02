package net.celloscope.aes.domain.model;

public record CryptoCommand(
        String data,
        String secretKey,
        String iv
) {
}
