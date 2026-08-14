package com.benbenlaw.abilitylock.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> showMillisecondsInTimer;
    public static final ModConfigSpec.ConfigValue<Boolean> showAbilityLockWorldCreation;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> knownWorldPresets;

    static {
        BUILDER.comment("BBL Ability Lock Client Config");

        BUILDER.push("Client Configs");

        showMillisecondsInTimer = BUILDER.comment("Show milliseconds in the speedrun timer")
                .define("showMillisecondsInTimer", true);

        showAbilityLockWorldCreation = BUILDER.comment(
                        "Show the AbilityLock world creation options - the shortcut button on " +
                                "Select World, and the AbilityLock tab on Create World.")
                .define("showAbilityLockWorldCreation", true);

        knownWorldPresets = BUILDER
                .comment("List of known world presets that can be selected from the Create World Screen.")
                .defineList("knownWorldPresets",
                        List.of("abilitylock:standard", "abilitylock:hardcore"),
                        () -> "abilitylock:standard",
                        obj -> obj instanceof String);

        BUILDER.pop();

        //End
        SPEC = BUILDER.build();
    }
}