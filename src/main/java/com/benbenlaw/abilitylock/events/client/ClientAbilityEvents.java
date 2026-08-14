package com.benbenlaw.abilitylock.events.client;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.ability.abilities.ScreenOpenAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "abilitylock", value = Dist.CLIENT)
public class ClientAbilityEvents {

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Screen newScreen = event.getNewScreen();
        if (newScreen == null) return;

        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof ScreenOpenAbility screenOpenAbility)) continue;

            AbilityData data = ability.getData();
            if (data == null) continue;

            if (screenOpenAbility.blocksScreen(player, newScreen, data)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
