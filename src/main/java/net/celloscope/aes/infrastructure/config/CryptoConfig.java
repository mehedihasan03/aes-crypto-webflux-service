package net.celloscope.aes.infrastructure.config;

import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class CryptoConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean(destroyMethod = "dispose")
    public Scheduler cryptoScheduler(CryptoSchedulerProperties properties) {
        int threadCap = properties.threadCap() == 0
                ? Math.max(4, Runtime.getRuntime().availableProcessors())
                : properties.threadCap();
        return Schedulers.newBoundedElastic(threadCap, properties.queueCapacity(), "aes-crypto");
    }
}
