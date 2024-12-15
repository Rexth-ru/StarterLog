package org.example.starterloghttp.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.example.starterloghttp.aspect.LogAspect;
import org.example.starterloghttp.filter.HttpLoggingFilter;
import org.example.starterloghttp.properties.HttpLoggingProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(HttpLoggingProperties.class)
@ConditionalOnClass(HttpLoggingProperties.class)
@ConditionalOnProperty(prefix = "http.logging", name = "enabled", havingValue = "true")
public class HttpLoggingAutoConfiguration {

    @Bean
    public LogAspect httpLoggingAspect(HttpLoggingProperties properties) {
        return new LogAspect(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "http.logging", name = "logHttp", havingValue = "true")
    public FilterRegistrationBean<HttpLoggingFilter> loggingFilter(HttpLoggingProperties properties) {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpLoggingFilter(properties));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}