package dev.project.bedtimestory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    private List<String> allowedOrigins;
    private String frontUrl;
    private String loginSuccessUrl;
    private String jwtSecretKey;
    private int jwtAccessTokenExpiration;
    private int jwtRefreshTokenExpiration;
    private String jwtAccessTokenName;
    private String jwtRefreshTokenName;
}