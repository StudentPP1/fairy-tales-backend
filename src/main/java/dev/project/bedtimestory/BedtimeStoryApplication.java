package dev.project.bedtimestory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class BedtimeStoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(BedtimeStoryApplication.class, args);
    }
}
