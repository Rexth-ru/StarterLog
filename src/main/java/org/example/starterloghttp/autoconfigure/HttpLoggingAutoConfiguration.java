package org.example.starterloghttp.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.example.starterloghttp.aspect.LogAspect;
import org.example.starterloghttp.config.LoggingConfig;
import org.example.starterloghttp.properties.HttpLoggingProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(HttpLoggingProperties.class)
@ConditionalOnClass(HttpLoggingProperties.class)
@ConditionalOnProperty(prefix = "http.logging", name = "enabled", havingValue = "true")
public class HttpLoggingAutoConfiguration {

    @Bean
    public LoggingConfig loggingConfig(final HttpLoggingProperties properties) {
        return new LoggingConfig(properties);
    }

    @Bean
    public LogAspect httpLoggingAspect(final LoggingConfig loggingConfig) {
        return new LogAspect(loggingConfig);
    }

}