package dev.project.bedtimestory.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

public class RedisContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> redis = new GenericContainer<>("redis")
            .withExposedPorts(6379);

    static {
        redis.start();
        Runtime.getRuntime().addShutdownHook(new Thread(redis::stop));
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.data.redis.host=" + redis.getHost(),
                "spring.data.redis.port=" + redis.getMappedPort(6379)
        ).applyTo(applicationContext.getEnvironment());
    }
}