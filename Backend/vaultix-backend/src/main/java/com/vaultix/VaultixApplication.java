package com.vaultix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VaultixApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultixApplication.class, args);
    }

}