package dev.project.bedtimestory.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    @Bean
    public Executor asyncTaskExecutor() {
        return new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(5)
                .maxPoolSize(20)
                .queueCapacity(500)
                .threadNamePrefix("AsyncExecutor-")
                .build();
    }
}