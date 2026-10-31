package com.mythicvisuals;

import com.mythicvisuals.core.hud.DirectionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionUtilTest {
    @Test
    void cardinals() {
        assertEquals("S", DirectionUtil.compass(0));
        assertEquals("W", DirectionUtil.compass(90));
        assertEquals("N", DirectionUtil.compass(180));
        assertEquals("E", DirectionUtil.compass(270));
    }

    @Test
    void diagonals() {
        assertEquals("SW", DirectionUtil.compass(45));
        assertEquals("NW", DirectionUtil.compass(135));
        assertEquals("NE", DirectionUtil.compass(225));
        assertEquals("SE", DirectionUtil.compass(315));
    }

    @Test
    void wrapsAround() {
        assertEquals("S", DirectionUtil.compass(360));
        assertEquals("S", DirectionUtil.compass(-1));
        assertEquals("N", DirectionUtil.compass(180 + 720));
    }

    @Test
    void axis() {
        assertEquals("+Z", DirectionUtil.axis(0));
        assertEquals("-X", DirectionUtil.axis(90));
        assertEquals("-Z", DirectionUtil.axis(180));
        assertEquals("+X", DirectionUtil.axis(270));
    }
}
