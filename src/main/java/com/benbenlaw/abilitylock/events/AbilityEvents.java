package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.ability.abilities.*;
import com.benbenlaw.abilitylock.events.client.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class AbilityEvents {

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;

        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof BlockBreakAbility blockBreakAbility)) continue;

            AbilityData data = ability.getData();
            if (data == null) continue;

            blockBreakAbility.onBlockBreak(event, data);
        }
    }

    @SubscribeEvent
    public static void onDimensionalTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof DimensionTravelAbility dimensionTravel)) continue;
            if (event.getEntity() instanceof Player player) {
                AbilityData data = ability.getData();
                if (data == null) continue;
                dimensionTravel.onDimensionTravel(event, player, event.getDimension(), data);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND) return;
        if (event.getEntity().level().isClientSide()) return;

        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof BlockInteractAbility blockInteract)) continue;

            AbilityData data = ability.getData();
            if (data == null) continue;

            blockInteract.onRightClickBlock(event, data);
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof EntityHurtAbility entityHurt)) continue;

            AbilityData data = ability.getData();
            if (data == null) continue;

            entityHurt.onEntityDamage(event, data);
        }
    }
}