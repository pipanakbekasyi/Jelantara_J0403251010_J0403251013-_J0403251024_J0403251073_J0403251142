package com.example.demo.service;

// =========================================================================
// KONSEP OOP PADA SERVICE INI:
// 1. ENCAPSULATION (Enkapsulasi): Struktur pelacakan percobaan login gagal
//    (AttemptRecord) dan peta concurrent disimpan secara 'private' di dalam
//    kelas agar aturan limit 5x/menit & cooldown 30 detik terjaga konsisten.
// 2. SINGLE RESPONSIBILITY PRINCIPLE (SRP): Kelas ini memiliki satu tanggung
//    jawab khusus, yaitu proteksi rate limiting pada proses autentikasi.
// =========================================================================
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000L;    // 1 menit (60 detik)
    private static final long COOLDOWN_MS = 30_000L;  // 30 detik cooldown

    private static class AttemptState {
        final Deque<Long> failedTimestamps = new ArrayDeque<>();
        long blockedUntil = 0L;
    }

    private final ConcurrentHashMap<String, AttemptState> attemptsMap = new ConcurrentHashMap<>();

    public synchronized boolean isBlocked(String key) {
        AttemptState state = attemptsMap.get(key);
        if (state == null) return false;
        long now = System.currentTimeMillis();
        if (state.blockedUntil > now) {
            return true;
        }
        // Cooldown selesai, reset block status
        if (state.blockedUntil > 0 && state.blockedUntil <= now) {
            state.blockedUntil = 0L;
            state.failedTimestamps.clear();
        }
        return false;
    }

    public synchronized long getRemainingCooldownSeconds(String key) {
        AttemptState state = attemptsMap.get(key);
        if (state == null) return 0L;
        long remainingMs = state.blockedUntil - System.currentTimeMillis();
        if (remainingMs <= 0) return 0L;
        return (long) Math.ceil(remainingMs / 1000.0);
    }

    public synchronized int recordFailedAttempt(String key) {
        long now = System.currentTimeMillis();
        AttemptState state = attemptsMap.computeIfAbsent(key, k -> new AttemptState());

        // Hapus catatan gagal yang lebih lama dari 1 menit
        while (!state.failedTimestamps.isEmpty() && (now - state.failedTimestamps.peekFirst()) > WINDOW_MS) {
            state.failedTimestamps.pollFirst();
        }

        state.failedTimestamps.addLast(now);

        if (state.failedTimestamps.size() >= MAX_ATTEMPTS) {
            state.blockedUntil = now + COOLDOWN_MS;
            state.failedTimestamps.clear();
            return 0; // Sisa percobaan 0 (terkunci 30 detik)
        }
        return MAX_ATTEMPTS - state.failedTimestamps.size();
    }

    public synchronized void resetAttempts(String key) {
        attemptsMap.remove(key);
    }
}
