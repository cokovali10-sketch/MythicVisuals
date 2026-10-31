package com.mythicvisuals;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 smoke test: brand constants are sane. Runs without Minecraft.
 */
class BrandingTest {

    @Test
    void brandConstantsPresent() {
        assertFalse(Branding.MOD_ID.isBlank());
        assertFalse(Branding.MOD_NAME.isBlank());
        assertFalse(Branding.MOD_VERSION.isBlank());
        assertTrue(Branding.MOD_ID.matches("[a-z0-9_]+"));
    }
}
