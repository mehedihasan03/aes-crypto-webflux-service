package net.celloscope.aes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AesCryptoWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(AesCryptoWebfluxApplication.class, args);
    }
}
