package net.celloscope.aes.adapter.in.web.dto.response;

public record CryptoResponse(
        String result,
        String algorithm,
        CryptoMetadata metadata
) {
}
