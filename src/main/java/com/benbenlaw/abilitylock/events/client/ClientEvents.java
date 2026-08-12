package com.benbenlaw.abilitylock.events.client;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.command.AbilityLockClientCommand;
import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.screen.AbilityLockScreen;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import com.benbenlaw.abilitylock.screen.TaskScreen;
import com.benbenlaw.abilitylock.util.KeyBinds;
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
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        AbilityLockClientCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onClientPress(InputEvent.Key event) {

        Screen currentScreen = Minecraft.getInstance().screen;

        if (currentScreen == null) {

            if (event.getKeyEvent().key() == KeyBinds.TASK_SCREEN_HOTKEY.getKey().getValue()) {
                Minecraft.getInstance().setScreen(new TaskScreen());
            }

            if (event.getKeyEvent().key() == KeyBinds.ABILITY_SCREEN_HOTKEY.getKey().getValue()) {
                Minecraft.getInstance().setScreen(new AbilityLockScreen());
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
        if (!SpeedrunTimer.isRunning() && !SpeedrunTimer.isFinished()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphicsExtractor guiGraphics = event.getGuiGraphics();
        String time = SpeedrunTimer.getFormattedTime();
        int width = mc.getWindow().getGuiScaledWidth();
        int x = (width / 2) - (mc.font.width(time) / 2);
        int y = 10;

        guiGraphics.text(mc.font, time, x, y, 0xFFFFFFFF);
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        SpeedrunTimer.onWorldJoin();
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SpeedrunTimer.save();
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!ClientConfig.showAbilityLockWorldCreation.get()) return;

        if (event.getScreen() instanceof SelectWorldScreen screen) {
            int width = 110;
            int x = screen.width - width - 5;
            int y = 6;

            Button button = Button.builder(Component.literal("Create AbilityLock World"), (b) -> {
                PendingAbilityLockWorldSettings.requestTabOnNextScreen();
                CreateWorldScreen.openFresh(screen.getMinecraft(), () -> screen.getMinecraft().setScreen(screen));
            }).bounds(x, y, width, 20).build();

            event.addListener(button);
        }
    }
}