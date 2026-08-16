package com.acme.salary.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "acme")
public record AcmeProperties(Jwt jwt, Seed seed, Cors cors) {

    public AcmeProperties {
        if (cors == null) {
            cors = new Cors(
                    List.of(
                            "http://localhost:5173",
                            "http://localhost:8081",
                            "http://localhost:80",
                            "http://localhost"),
                    List.of("https://*.vercel.app"));
        }
    }

    public record Jwt(String secret, long ttlHours) {
    }

    public record Seed(boolean enabled, int employees, String demoPassword) {
    }

    public record Cors(List<String> origins, List<String> originPatterns) {
        public Cors {
            origins = origins == null ? List.of() : List.copyOf(origins);
            originPatterns = originPatterns == null ? List.of() : List.copyOf(originPatterns);
        }
    }
}
