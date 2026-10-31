package com.mythicvisuals.modules.debug;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.Module;
import com.mythicvisuals.module.setting.BooleanSetting;

/**
 * Phase 2 verification module: proves register/toggle/settings/save/load.
 * Logs on toggle when verbose is on. No gameplay effect.
 */
public class DebugModule extends Module {
    private final BooleanSetting verbose;

    public DebugModule() {
        super("debug", "Debug", "Lifecycle verification helper (no gameplay effect).",
                Category.UTILITY);
        this.verbose = setting(new BooleanSetting("verbose",
                "Log a line on every toggle.", false));
    }

    public boolean verbose() {
        return verbose.get();
    }

    @Override
    public void onEnable() {
        if (verbose.get()) {
            logger.info("[mythicvisuals] debug module enabled.");
        }
    }

    @Override
    public void onDisable() {
        if (verbose.get()) {
            logger.info("[mythicvisuals] debug module disabled.");
        }
    }
}
