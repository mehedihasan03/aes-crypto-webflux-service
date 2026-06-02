package net.celloscope.aes.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EncryptRequest(
        @NotNull(message = "data is required")
        String data,

        @NotBlank(message = "secretKey is required")
        String secretKey,

        String iv
) implements CryptoRequest {
}
