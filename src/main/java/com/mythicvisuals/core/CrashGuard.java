package com.mythicvisuals.core;

import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Crash-proofing helpers: throttled warnings and per-key failure counters.
 * A repeatedly failing HUD element/input handler gets disabled instead of
 * spamming the log or killing the frame. Pure logic, unit tested.
 */
public final class CrashGuard {
    private static final Map<String, AtomicInteger> FAILURES = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> WARNED = new ConcurrentHashMap<>();

    private CrashGuard() {
    }

    /** Logs the first occurrence only; returns true if this was the first. */
    public static boolean warnOnce(Logger log, String key, String message, Throwable t) {
        if (WARNED.putIfAbsent(key, Boolean.TRUE) != null) {
            return false;
        }
        if (t == null) {
            log.warn("[mythicvisuals] {} [{}]", message, key);
        } else {
            log.warn("[mythicvisuals] {} [{}]", message, key, t);
        }
        return true;
    }

    /**
     * Records a failure. Returns true while the key is still allowed
     * (failures {@code <} maxAllowed), false once it must be disabled.
     */
    public static boolean noteFailure(String key, int maxAllowed) {
        int n = FAILURES.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        return n < maxAllowed;
    }

    public static int failures(String key) {
        AtomicInteger n = FAILURES.get(key);
        return n == null ? 0 : n.get();
    }

    public static void reset(String key) {
        FAILURES.remove(key);
        WARNED.remove(key);
    }

    /** Test/dev seam: clears all state. */
    static void resetAll() {
        FAILURES.clear();
        WARNED.clear();
    }
}
