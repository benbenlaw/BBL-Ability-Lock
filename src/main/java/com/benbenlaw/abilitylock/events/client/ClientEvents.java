package com.benbenlaw.abilitylock.events.client;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.client.AbilityUnlockToastManager;
import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.screen.AbilityLockScreen;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import com.benbenlaw.abilitylock.screen.TaskScreen;
import com.benbenlaw.abilitylock.util.KeyBinds;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = AbilityLock.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientPress(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Screen currentScreen = mc.screen;
        boolean ownScreenOpen = currentScreen instanceof TaskScreen || currentScreen instanceof AbilityLockScreen;

        if (currentScreen != null && !ownScreenOpen) return;

        boolean taskKey = event.getKey() == KeyBinds.TASK_SCREEN_HOTKEY.getKey().getValue();
        boolean abilityKey = event.getKey() == KeyBinds.ABILITY_SCREEN_HOTKEY.getKey().getValue();

        if (taskKey) {
            if (currentScreen instanceof TaskScreen) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new TaskScreen());
            }
        }

        if (abilityKey) {
            if (currentScreen instanceof AbilityLockScreen) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new AbilityLockScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(RegisterKeyMappingsEvent event) {
        event.register(KeyBinds.TASK_SCREEN_HOTKEY);
        event.register(KeyBinds.ABILITY_SCREEN_HOTKEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        SpeedrunTimer.tick();

        if (player != null && !SpeedrunTimer.isRunning() && !SpeedrunTimer.isFinished()) {
            if (player.input.keyPresses.forward() || player.input.keyPresses.backward() || player.input.keyPresses.left()
                    || player.input.keyPresses.right()) {
                SpeedrunTimer.start();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();

        if (SpeedrunTimer.isRunning() || SpeedrunTimer.isFinished()) {
            String time = SpeedrunTimer.getFormattedTime();
            int timeX = (width / 2) - (mc.font.width(time) / 2);
            guiGraphics.text(mc.font, time, timeX, 10, 0xFFFFFFFF);
        }

        AbilityUnlockToastManager.Entry entry = AbilityUnlockToastManager.current();
        if (entry != null) {
            int labelX = (width / 2) - (mc.font.width(entry.label()) / 2);
            int color = entry.bonus() ? 0xFFFFD700 : 0xFF55FF55;
            guiGraphics.text(mc.font, entry.label(), labelX, 30, color);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        SpeedrunTimer.onWorldJoin();
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SpeedrunTimer.save();
    }
}