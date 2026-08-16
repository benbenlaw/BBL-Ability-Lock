package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.presets.PresetData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PresetBuilder {

    private final String displayName;
    private final List<Identifier> startingAbilities = new ArrayList<>();
    private boolean onDeathLoseWorld = false;
    private final List<Identifier> unlockableAbilities = new ArrayList<>();
    private Integer defaultGridSize;
    private Integer immediateTaskPercentage;
    private int bonusAbilityPercentage = 0;

    private PresetBuilder(String displayName) {
        this.displayName = displayName;
    }

    public static PresetBuilder preset(String displayName) {
        return new PresetBuilder(displayName);
    }

    public PresetBuilder startingAbility(Identifier id) {
        this.startingAbilities.add(id);
        return this;
    }

    public PresetBuilder startingAbility(String path) {
        return startingAbility(AbilityLock.identifier(path));
    }

    public PresetBuilder startingAbilities(String... paths) {
        for (String path : paths) startingAbility(path);
        return this;
    }

    public PresetBuilder onDeathLoseWorld(boolean value) {
        this.onDeathLoseWorld = value;
        return this;
    }

    public PresetBuilder unlockableAbility(Identifier id) {
        this.unlockableAbilities.add(id);
        return this;
    }

    public PresetBuilder unlockableAbility(String path) {
        return unlockableAbility(AbilityLock.identifier(path));
    }

    public PresetBuilder unlockableAbilities(String... paths) {
        for (String path : paths) unlockableAbility(path);
        return this;
    }

    public PresetBuilder defaultGridSize(int size) {
        this.defaultGridSize = size;
        return this;
    }

    public PresetBuilder immediateTaskPercentage(int percent) {
        this.immediateTaskPercentage = percent;
        return this;
    }

    public PresetBuilder bonusAbilityPercentage(int percent) {
        this.bonusAbilityPercentage = percent;
        return this;
    }

    public PresetData build() {
        return new PresetData(
                displayName,
                List.copyOf(startingAbilities),
                onDeathLoseWorld,
                List.copyOf(unlockableAbilities),
                Optional.ofNullable(defaultGridSize),
                Optional.ofNullable(immediateTaskPercentage),
                bonusAbilityPercentage
        );
    }
}