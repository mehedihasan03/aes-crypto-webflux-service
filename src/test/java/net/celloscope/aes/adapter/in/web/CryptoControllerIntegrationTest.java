package net.celloscope.aes.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import net.celloscope.aes.adapter.in.web.dto.response.CryptoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CryptoControllerIntegrationTest {

    private static final String AES_256_KEY = "0123456789abcdef0123456789abcdef";

    private final WebTestClient webTestClient;

    @Autowired
    CryptoControllerIntegrationTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void encryptAndDecryptOverHttp() {
        CryptoResponse encrypted = webTestClient.post()
                .uri("/api/v1/crypto/aes/encrypt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "data", "Hello from WebFlux",
                        "secretKey", AES_256_KEY
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CryptoResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(encrypted).isNotNull();
        assertThat(encrypted.algorithm()).isEqualTo("AES/GCM/NoPadding");
        assertThat(encrypted.metadata().iv()).isNotBlank();

        webTestClient.post()
                .uri("/api/v1/crypto/aes/decrypt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "data", encrypted.result(),
                        "secretKey", AES_256_KEY,
                        "iv", encrypted.metadata().iv()
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo("Hello from WebFlux")
                .jsonPath("$.algorithm").isEqualTo("AES/GCM/NoPadding")
                .jsonPath("$.metadata.iv").isEqualTo(encrypted.metadata().iv());
    }

    @Test
    void validationErrorsReturnBadRequestBody() {
        webTestClient.post()
                .uri("/api/v1/crypto/aes/encrypt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Request validation failed")
                .jsonPath("$.details").isArray();
    }
}
