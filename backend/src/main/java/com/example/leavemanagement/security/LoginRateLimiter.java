package com.example.leavemanagement.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {
    private final Map<String, Attempt> attempts = new HashMap<>();
    private final int threshold;
    private final Duration window;
    private final int maxEntries;
    private final Clock clock = Clock.systemUTC();

    public LoginRateLimiter(@Value("${security.login-throttle.threshold:5}") int threshold,
                            @Value("${security.login-throttle.window:PT1M}") Duration window,
                            @Value("${security.login-throttle.max-entries:10000}") int maxEntries) {
        this.threshold = threshold; this.window = window; this.maxEntries = maxEntries;
    }

    public synchronized boolean isBlocked(String account, String source) {
        Attempt attempt = current(key(account, source));
        return attempt != null && attempt.blockedUntil().isAfter(Instant.now(clock));
    }

    public synchronized boolean recordFailure(String account, String source) {
        String key = key(account, source); Instant now = Instant.now(clock); Attempt current = current(key);
        int failures = current == null ? 1 : current.failures() + 1;
        attempts.put(key, new Attempt(failures, failures >= threshold ? now.plus(window) : now));
        trim();
        return failures >= threshold;
    }

    public synchronized void reset(String account, String source) { attempts.remove(key(account, source)); }

    private Attempt current(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt != null && !attempt.blockedUntil().isAfter(Instant.now(clock)) && attempt.failures() >= threshold) attempts.remove(key);
        return attempts.get(key);
    }

    private void trim() { while (attempts.size() > maxEntries) attempts.remove(attempts.keySet().iterator().next()); }
    private String key(String account, String source) { return account.trim().toLowerCase(java.util.Locale.ROOT) + "\u0000" + source; }
    private record Attempt(int failures, Instant blockedUntil) { }
}