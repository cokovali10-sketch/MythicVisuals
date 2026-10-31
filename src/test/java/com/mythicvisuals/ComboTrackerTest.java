package com.mythicvisuals;

import com.mythicvisuals.modules.combat.ComboTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComboTrackerTest {
    @Test
    void countsConsecutiveHits() {
        ComboTracker t = new ComboTracker();
        t.hit(1000);
        t.hit(1500);
        t.hit(2000);
        assertEquals(3, t.combo(2000));
    }

    @Test
    void expiresAfterPause() {
        ComboTracker t = new ComboTracker();
        t.hit(0);
        assertEquals(1, t.combo(1000));
        assertEquals(0, t.combo(3001));
        // next hit starts a fresh combo
        t.hit(4000);
        assertEquals(1, t.combo(4000));
    }

    @Test
    void resetsOnHurt() {
        ComboTracker t = new ComboTracker();
        t.hit(1000);
        t.hit(1500);
        t.onHurt();
        assertEquals(0, t.combo(1500));
    }

    @Test
    void emptyByDefault() {
        assertEquals(0, new ComboTracker().combo(99999));
    }
}
