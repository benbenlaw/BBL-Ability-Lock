package com.benbenlaw.abilitylock.util;

import com.benbenlaw.abilitylock.AbilityLock;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class KeyBinds {

    public static final KeyMapping.Category KEY_CATEGORY =  KeyMapping.Category.register(AbilityLock.identifier("abilitylock"));

    public static final String TASK_SCREEN_KEY = "key.abilitylock.task_screen_key";
    public static final String ABILITY_SCREEN_KEY = "key.abilitylock.ability_screen_key";

    public static final KeyMapping TASK_SCREEN_HOTKEY = new KeyMapping(TASK_SCREEN_KEY, KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, InputConstants.KEY_G, KEY_CATEGORY);
    public static final KeyMapping ABILITY_SCREEN_HOTKEY = new KeyMapping(ABILITY_SCREEN_KEY, KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, InputConstants.KEY_H, KEY_CATEGORY);
}
