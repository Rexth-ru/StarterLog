package org.example.starterloghttp.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "http.logging")
public class HttpLoggingProperties {
    private boolean enabled;
    private String level;
    private boolean logRequestHeaders;
    private boolean logResponseHeaders;
    private boolean logHttp;

    public boolean isErrorLevel() {
        return "error".equalsIgnoreCase(level);
    }

    public boolean isInfoLevel() {
        return "info".equalsIgnoreCase(level);
    }
}

