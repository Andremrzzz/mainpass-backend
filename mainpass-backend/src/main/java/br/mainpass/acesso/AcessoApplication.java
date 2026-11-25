package br.mainpass.acesso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AcessoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcessoApplication.class, args);
    }
}
