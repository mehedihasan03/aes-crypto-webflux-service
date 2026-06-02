package net.celloscope.aes.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DecryptRequest(
        @NotBlank(message = "data is required")
        String data,

        @NotBlank(message = "secretKey is required")
        String secretKey,

        @NotBlank(message = "iv is required for AES-GCM decryption")
        String iv
) {
}
