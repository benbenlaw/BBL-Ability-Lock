package com.benbenlaw.abilitylock.screen;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public interface AbilityLockGameTabAccess {

    @Nullable
    Identifier abilityLock$getSelectedPreset();

    int abilityLock$getSelectedGridWidth();

    int abilityLock$getSelectedGridHeight();
}