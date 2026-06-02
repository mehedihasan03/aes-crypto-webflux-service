package net.celloscope.aes.domain.model;

public record CryptoOperationResult(
        String result,
        String algorithm,
        String iv,
        int tagLengthBits
) {
}
