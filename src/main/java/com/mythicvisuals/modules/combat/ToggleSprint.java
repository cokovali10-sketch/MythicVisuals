package com.mythicvisuals.modules.combat;

import com.mythicvisuals.module.Category;
import com.mythicvisuals.module.ClientTickable;
import com.mythicvisuals.module.Module;
import net.minecraft.client.Minecraft;

/** Forces sprint while moving forward (no double-W). */
public class ToggleSprint extends Module implements ClientTickable {
    public ToggleSprint() {
        super("toggle_sprint", "ToggleSprint", "Always sprint while moving forward", Category.COMBAT);
    }

    @Override
    public void clientTick(Minecraft client) {
        if (client.player == null) {
            return;
        }
        if (client.options.keyUp.isDown()
                && !client.player.isCrouching()
                && !client.player.isInWater()
                && client.player.getFoodData().getFoodLevel() > 6) {
            client.player.setSprinting(true);
        }
    }
}
