package com.benbenlaw.abilitylock.mixin.client;

import com.benbenlaw.abilitylock.config.ClientConfig;
import com.benbenlaw.abilitylock.screen.AbilityLockCreateWorldTab;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Shadow
    @Final
    private WorldCreationUiState uiState;

    @Shadow
    private @Nullable TabNavigationBar tabNavigationBar;

    @Unique
    private AbilityLockCreateWorldTab abilityLock$tab;

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;addTabs([Lnet/minecraft/client/gui/components/tabs/Tab;)Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;"
            )
    )
    private Tab[] abilityLock$addTab(Tab[] tabs) {
        if (!ClientConfig.showAbilityLockWorldCreation.get()) {
            return tabs;
        }

        this.abilityLock$tab = new AbilityLockCreateWorldTab();
        Tab[] result = Arrays.copyOf(tabs, tabs.length + 1);
        result[tabs.length] = this.abilityLock$tab;
        return result;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void abilityLock$selectTabIfRequested(CallbackInfo ci) {
        if (this.tabNavigationBar == null || this.abilityLock$tab == null) return;
        if (!PendingAbilityLockWorldSettings.consumeTabRequest()) return;

        int index = this.tabNavigationBar.getTabs().indexOf(this.abilityLock$tab);
        if (index != -1) {
            this.tabNavigationBar.selectTab(index, false);
        }
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void abilityLock$stashSettings(CallbackInfo ci) {
        if (this.abilityLock$tab == null) return;

        String levelName = this.uiState.getName().trim();
        if (levelName.isEmpty()) return;

        PendingAbilityLockWorldSettings.set(
                levelName,
                this.abilityLock$tab.getSelectedPreset(),
                this.abilityLock$tab.getSelectedGridSize()
        );
    }
}