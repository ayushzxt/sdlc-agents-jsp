package com.example.leavemanagement.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LoginRateLimiterTest {
    @Test
    void blocksAtThresholdAndResetClearsAccountSourceBucket() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, Duration.ofMinutes(1), 10);

        assertThat(limiter.recordFailure("User@example.com", "127.0.0.1")).isFalse();
        assertThat(limiter.recordFailure("user@example.com", "127.0.0.1")).isFalse();
        assertThat(limiter.recordFailure("user@example.com", "127.0.0.1")).isTrue();
        assertThat(limiter.isBlocked("user@example.com", "127.0.0.1")).isTrue();

        limiter.reset("user@example.com", "127.0.0.1");

        assertThat(limiter.isBlocked("user@example.com", "127.0.0.1")).isFalse();
        assertThat(limiter.recordFailure("user@example.com", "127.0.0.1")).isFalse();
    }

    @Test
    void keepsAccountsAndSourcesSeparate() {
        LoginRateLimiter limiter = new LoginRateLimiter(1, Duration.ofMinutes(1), 10);

        assertThat(limiter.recordFailure("one@example.com", "127.0.0.1")).isTrue();
        assertThat(limiter.isBlocked("two@example.com", "127.0.0.1")).isFalse();
        assertThat(limiter.isBlocked("one@example.com", "127.0.0.2")).isFalse();
    }
}