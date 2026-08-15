package com.benbenlaw.abilitylock.mixin.client;

import com.benbenlaw.abilitylock.screen.AbilityLockGameTabAccess;
import com.benbenlaw.abilitylock.screen.PendingAbilityLockWorldSettings;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Shadow
    @Final
    private WorldCreationUiState uiState;

    @Shadow
    private @Nullable TabNavigationBar tabNavigationBar;

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void abilityLock$stashSettings(CallbackInfo ci) {
        if (this.tabNavigationBar == null) return;

        String levelName = this.uiState.getName().trim();
        if (levelName.isEmpty()) return;

        for (Tab tab : this.tabNavigationBar.getTabs()) {
            if (tab instanceof AbilityLockGameTabAccess access) {
                PendingAbilityLockWorldSettings.set(
                        levelName,
                        access.abilityLock$getSelectedPreset(),
                        access.abilityLock$getSelectedGridWidth(),
                        access.abilityLock$getSelectedGridHeight()
                );
                return;
            }
        }
    }
}