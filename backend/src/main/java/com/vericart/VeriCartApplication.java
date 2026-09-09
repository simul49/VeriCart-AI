package com.vericart;

import com.vericart.util.DotEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VeriCartApplication {
    public static void main(String[] args) {
        // Load .env (gitignored secrets) into system properties BEFORE the context
        // starts, so ${...} placeholders in application.yml resolve to real keys.
        DotEnvLoader.load();
        SpringApplication.run(VeriCartApplication.class, args);
        System.out.println("========================================");
        System.out.println("  VeriCart AI — Trust-Driven Commerce");
        System.out.println("  Running on http://localhost:8080");
        System.out.println("========================================");
    }
}
