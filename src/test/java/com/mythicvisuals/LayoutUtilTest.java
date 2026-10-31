package com.mythicvisuals;

import com.mythicvisuals.gui.LayoutUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutUtilTest {
    @Test
    void keepsInside() {
        assertArrayEquals(new int[]{100, 50}, LayoutUtil.clamp(100, 50, 60, 20, 800, 600));
        // off left/top snaps to margin
        assertArrayEquals(new int[]{2, 2}, LayoutUtil.clamp(-50, -10, 60, 20, 800, 600));
        // off right/bottom snaps inside
        assertArrayEquals(new int[]{738, 578}, LayoutUtil.clamp(900, 700, 60, 20, 800, 600));
    }

    @Test
    void hits() {
        int[] b = {10, 20, 60, 20};
        assertTrue(LayoutUtil.hits(b, 10, 20));
        assertTrue(LayoutUtil.hits(b, 69, 39));
        assertFalse(LayoutUtil.hits(b, 70, 39));
        assertFalse(LayoutUtil.hits(b, 0, 0));
    }
}
