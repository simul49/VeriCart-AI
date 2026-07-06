package com.vericart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VeriCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(VeriCartApplication.class, args);
        System.out.println("========================================");
        System.out.println("  VeriCart AI — Trust-Driven Commerce");
        System.out.println("  Running on http://localhost:8080");
        System.out.println("========================================");
    }
}
