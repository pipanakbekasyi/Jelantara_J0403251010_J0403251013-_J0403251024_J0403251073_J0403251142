package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Kelas utama untuk menjalankan aplikasi Spring Boot Jelantara
@SpringBootApplication
public class DemoApplication {
    // Fungsi main untuk menjalankan embedded server
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
