package com.example.demo.service;

// =========================================================================
// KONSEP OOP PADA SERVICE INI:
// 1. ENCAPSULATION (Enkapsulasi): Menyembunyikan detail algoritma hashing
//    SHA-256 + salt acak 16-byte di balik method public encodePassword()
//    dan matchesPassword().
// 2. ABSTRACTION (Abstraksi): Menyediakan antarmuka enkripsi & verifikasi
//    password yang bersih untuk Controller dan JelantaraService.
// =========================================================================
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class PasswordEncoderService {

    private static final String PREFIX = "jlt$sha256$";
    private final SecureRandom secureRandom = new SecureRandom();

    public boolean isEncoded(String password) {
        return password != null && password.startsWith(PREFIX);
    }

    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return null;
        }
        if (isEncoded(rawPassword)) {
            return rawPassword;
        }
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = sha256(salt + ":" + rawPassword);
        return PREFIX + salt + "$" + hash;
    }

    public String generateGooglePlaceholderPassword() {
        return encodePassword("GOOGLE_OAUTH_" + UUID.randomUUID());
    }

    public boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || rawPassword.isBlank() || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            String withoutPrefix = storedPassword.substring(PREFIX.length());
            int sepIdx = withoutPrefix.indexOf('$');
            if (sepIdx <= 0) return false;
            String salt = withoutPrefix.substring(0, sepIdx);
            String expectedHash = withoutPrefix.substring(sepIdx + 1);
            String actualHash = sha256(salt + ":" + rawPassword);
            return MessageDigest.isEqual(
                    expectedHash.getBytes(StandardCharsets.UTF_8),
                    actualHash.getBytes(StandardCharsets.UTF_8)
            );
        }
        return storedPassword.equals(rawPassword);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Gagal mengenkripsi password", e);
        }
    }
}
