package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Locale;

/** Block / entity interaction range from attributes. */
public class ReachHud extends HudModule {
    public ReachHud() {
        super("reach", "Reach Display", "Block and entity interaction range", 4, 192);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        double block = client.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        double entity = client.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        return List.of(String.format(Locale.ROOT, "Reach: %.1f / %.1f", block, entity));
    }
}
