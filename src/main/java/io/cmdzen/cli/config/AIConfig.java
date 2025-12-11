package io.cmdzen.cli.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "openrouter")
public class AIConfig {

    private String apiKey;
    private String endpoint;
    private String model;

}
