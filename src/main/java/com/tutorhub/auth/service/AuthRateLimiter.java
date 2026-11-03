package com.tutorhub.auth.service;

import com.tutorhub.common.exception.BusinessRuleException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
@Service public class AuthRateLimiter {
    private static final int LIMIT = 5; private static final long WINDOW_SECONDS = 60;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    public void check(String action, String ip) { String key = action + ':' + ip; Instant now = Instant.now(); Window window = windows.compute(key, (ignored, old) -> old == null || old.started.plusSeconds(WINDOW_SECONDS).isBefore(now) ? new Window(now, 1) : new Window(old.started, old.count + 1)); if (window.count > LIMIT) throw new BusinessRuleException("RATE_LIMITED", "Too many requests"); }
    private record Window(Instant started, int count) {}
}
