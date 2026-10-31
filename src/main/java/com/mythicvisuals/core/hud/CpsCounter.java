package com.mythicvisuals.core.hud;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sliding-window click counter. Timestamps are injected so the logic
 * is unit testable without wall-clock access.
 */
public final class CpsCounter {
    /** Window in milliseconds. */
    public static final long WINDOW_MS = 1000L;

    private final Deque<Long> clicks = new ArrayDeque<>();

    public void record(long nowMs) {
        clicks.addLast(nowMs);
        prune(nowMs);
    }

    public int count(long nowMs) {
        prune(nowMs);
        return clicks.size();
    }

    private void prune(long nowMs) {
        while (!clicks.isEmpty() && nowMs - clicks.peekFirst() >= WINDOW_MS) {
            clicks.removeFirst();
        }
    }
}
