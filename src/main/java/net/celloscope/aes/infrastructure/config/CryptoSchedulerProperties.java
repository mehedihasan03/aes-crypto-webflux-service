package net.celloscope.aes.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "crypto.scheduler")
public record CryptoSchedulerProperties(
        @DefaultValue("0")
        int threadCap,

        @DefaultValue("1000")
        int queueCapacity
) {

    public CryptoSchedulerProperties {
        if (threadCap < 0) {
            throw new IllegalArgumentException("crypto.scheduler.thread-cap must be zero or greater");
        }

        if (queueCapacity < 1) {
            throw new IllegalArgumentException("crypto.scheduler.queue-capacity must be greater than zero");
        }
    }
}
