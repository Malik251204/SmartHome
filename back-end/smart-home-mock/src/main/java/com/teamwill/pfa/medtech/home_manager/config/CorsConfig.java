package com.teamwill.pfa.medtech.home_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// This backend has no auth at all — nothing to secure, so there's no
// reason to pull in Spring Security just to configure CORS. This is the
// plain Spring MVC equivalent: without it, the browser blocks the
// frontend's requests outright regardless of whether the API call itself
// is correct. Comma-separated origins are read from an env var so this
// doesn't need a code change to add a deployed frontend URL later.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
