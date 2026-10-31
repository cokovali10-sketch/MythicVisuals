package com.mythicvisuals;

import com.mythicvisuals.core.hud.CpsCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CpsCounterTest {
    @Test
    void countsClicksInsideWindow() {
        CpsCounter cps = new CpsCounter();
        cps.record(1000);
        cps.record(1200);
        cps.record(1500);
        assertEquals(3, cps.count(1500));
    }

    @Test
    void prunesOldClicks() {
        CpsCounter cps = new CpsCounter();
        cps.record(0);
        cps.record(500);
        cps.record(1500);
        // click at t=0 is exactly WINDOW_MS old at t=1000+... check at t=1500: only 500? no: 1500-500=1000 -> pruned
        assertEquals(1, cps.count(1500));
        assertEquals(0, cps.count(2600));
    }

    @Test
    void emptyByDefault() {
        assertEquals(0, new CpsCounter().count(99999));
    }
}
