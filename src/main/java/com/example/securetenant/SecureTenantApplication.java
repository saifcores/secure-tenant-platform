package com.example.securetenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureTenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureTenantApplication.class, args);
    }
}
