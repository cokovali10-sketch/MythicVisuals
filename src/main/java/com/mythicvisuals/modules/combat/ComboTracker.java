package com.mythicvisuals.modules.combat;

/** Pure consecutive-hits logic with expiry. Unit tested. */
public final class ComboTracker {
    /** Combo expires after this long without a hit. */
    public static final long EXPIRE_MS = 3000L;

    private int combo;
    private long lastHitMs = -1;

    public void hit(long nowMs) {
        if (lastHitMs >= 0 && nowMs - lastHitMs > EXPIRE_MS) {
            combo = 0;
        }
        combo++;
        lastHitMs = nowMs;
    }

    public void onHurt() {
        combo = 0;
        lastHitMs = -1;
    }

    public int combo(long nowMs) {
        if (lastHitMs >= 0 && nowMs - lastHitMs > EXPIRE_MS) {
            combo = 0;
            lastHitMs = -1;
        }
        return combo;
    }
}
