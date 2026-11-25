package br.mainpass.acesso.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades personalizadas lidas do bloco "app" do application.yml.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Qrcode qrcode, Cors cors) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Qrcode(long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
