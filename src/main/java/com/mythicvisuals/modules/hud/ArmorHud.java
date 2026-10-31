package com.mythicvisuals.modules.hud;

import com.mythicvisuals.core.hud.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorHud extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public ArmorHud() {
        super("armor", "Armor Status", "Equipped armor with durability left", 4, 116);
    }

    @Override
    protected List<String> lines(Minecraft client) {
        List<String> out = new ArrayList<>();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            String text = stack.getHoverName().getString();
            if (stack.getMaxDamage() > 0) {
                text += " " + (stack.getMaxDamage() - stack.getDamageValue());
            }
            out.add(text);
        }
        return out;
    }
}
