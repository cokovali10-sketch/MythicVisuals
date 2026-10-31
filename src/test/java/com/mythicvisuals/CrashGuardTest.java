package com.mythicvisuals;

import com.mythicvisuals.core.CrashGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrashGuardTest {
    @BeforeEach
    void reset() {
        CrashGuard.reset("k");
    }

    @Test
    void warnOnceOnly() {
        assertTrue(CrashGuard.warnOnce(LoggerFactory.getLogger("t"), "k", "boom", null));
        assertFalse(CrashGuard.warnOnce(LoggerFactory.getLogger("t"), "k", "boom", null));
    }

    @Test
    void failureBudget() {
        // maxAllowed=3: first two failures allowed, third disables
        assertTrue(CrashGuard.noteFailure("k", 3));
        assertTrue(CrashGuard.noteFailure("k", 3));
        assertFalse(CrashGuard.noteFailure("k", 3));
        assertEquals(3, CrashGuard.failures("k"));
    }

    @Test
    void unknownKeyZero() {
        assertEquals(0, CrashGuard.failures("nope"));
    }
}
