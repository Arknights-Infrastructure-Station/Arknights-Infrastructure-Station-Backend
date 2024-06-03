package com.arknightsinfrastructurestationbackend.service.email;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationAttemptService {
    private final ConcurrentHashMap<String, Integer> attemptCounter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> freezeTime = new ConcurrentHashMap<>();

    private final int maxAttempts = 3;
    private final int initialFreezeTimeInMinutes = 5; // 初始冻结时间5分钟

    private String generateKey(String email, String ipAddress) {
        return email + ":" + ipAddress;
    }

    public int recordFailedAttempt(String email, String ipAddress) {
        String key = generateKey(email, ipAddress);
        int attempts = attemptCounter.getOrDefault(key, 0) + 1;
        attemptCounter.put(key, attempts);

        if (attempts >= maxAttempts) {
            freezeEmail(key);
            return 0;
        }
        return maxAttempts - attempts;
    }

    public void freezeEmail(String key) {
        int attempts = attemptCounter.getOrDefault(key, 0);
        int freezeMultiplier = attempts / maxAttempts;
        int freezeDuration = (int) Math.pow(initialFreezeTimeInMinutes, freezeMultiplier);
        freezeTime.put(key, LocalDateTime.now().plusMinutes(freezeDuration));
    }

    public boolean isEmailFrozen(String email, String ipAddress) {
        String key = generateKey(email, ipAddress);
        LocalDateTime frozenUntil = freezeTime.getOrDefault(key, LocalDateTime.MIN);
        return frozenUntil.isAfter(LocalDateTime.now());
    }

    public long getRemainingFreezeTime(String email, String ipAddress) {
        String key = generateKey(email, ipAddress);
        LocalDateTime frozenUntil = freezeTime.getOrDefault(key, LocalDateTime.MIN);
        if (frozenUntil.isAfter(LocalDateTime.now())) {
            return LocalDateTime.now().until(frozenUntil, ChronoUnit.MINUTES);
        }
        return 0;
    }

    public void clearAttempts(String email, String ipAddress) {
        String key = generateKey(email, ipAddress);
        attemptCounter.remove(key);
        freezeTime.remove(key);
    }
}
