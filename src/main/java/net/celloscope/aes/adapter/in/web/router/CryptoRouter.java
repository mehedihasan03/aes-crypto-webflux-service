package net.celloscope.aes.adapter.in.web.router;

import net.celloscope.aes.adapter.in.web.handler.CryptoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class CryptoRouter {

    private static final String BASE_PATH = "/api/v1/crypto/aes";

    @Bean
    public RouterFunction<ServerResponse> cryptoRoutes(CryptoHandler cryptoHandler) {
        return RouterFunctions.route()
                .path(BASE_PATH, builder -> builder
                        .POST("/encrypt", cryptoHandler::encrypt)
                        .POST("/decrypt", cryptoHandler::decrypt))
                .build();
    }
}
