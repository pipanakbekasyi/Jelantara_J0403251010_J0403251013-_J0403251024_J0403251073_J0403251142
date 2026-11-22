package com.example.demo.service;

import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@(gmail\\.com|mail\\.com)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern REKENING_PATTERN = Pattern.compile("^[0-9]{5,25}$");

    private InputValidator() {}

    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email wajib diisi!");
        }
        String clean = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Format email tidak valid! Email wajib menggunakan domain @gmail.com atau @mail.com.");
        }
        return clean;
    }

    public static String validatePhone(String phone, String fieldLabel) {
        String label = (fieldLabel != null && !fieldLabel.isBlank()) ? fieldLabel : "Nomor telepon";
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException(label + " wajib diisi!");
        }
        String clean = phone.trim();
        if (!PHONE_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " hanya boleh berisi angka (10-15 digit) dan tidak boleh mengandung huruf atau simbol.");
        }
        return clean;
    }

    public static String validateRekening(String noRekening) {
        if (noRekening == null || noRekening.isBlank()) {
            throw new IllegalArgumentException("Nomor rekening / E-Wallet wajib diisi!");
        }
        String clean = noRekening.trim();
        if (!REKENING_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("Nomor rekening / E-Wallet hanya boleh berisi angka (5-25 digit) dan tidak boleh mengandung huruf atau simbol.");
        }
        return clean;
    }

    public static String validateNotBlank(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldLabel + " wajib diisi!");
        }
        return value.trim();
    }

    public static double validateMinDouble(Double value, double min, String fieldLabel) {
        if (value == null || value < min) {
            throw new IllegalArgumentException(fieldLabel + " harus berupa angka minimal " + (min == Math.floor(min) ? String.valueOf((long) min) : min) + ".");
        }
        return value;
    }

    public static int validateMinInt(Integer value, int min, String fieldLabel) {
        if (value == null || value < min) {
            throw new IllegalArgumentException(fieldLabel + " harus berupa angka bulat minimal " + min + ".");
        }
        return value;
    }
}
