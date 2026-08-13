package com.acme.salary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "acme")
public record AcmeProperties(Jwt jwt, Seed seed) {

    public record Jwt(String secret, long ttlHours) {
    }

    public record Seed(boolean enabled, int employees, String demoPassword) {
    }
}
