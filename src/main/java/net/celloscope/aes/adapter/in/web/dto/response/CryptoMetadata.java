package net.celloscope.aes.adapter.in.web.dto.response;

public record CryptoMetadata(
        String iv,
        int tagLengthBits
) {
}
