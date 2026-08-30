package com.benbenlaw.abilitylock.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> showMillisecondsInTimer;
    public static final ModConfigSpec.ConfigValue<Boolean> showAbilityLockWorldCreation;
    public static final ModConfigSpec.ConfigValue<String> defaultPreset;

    static {
        BUILDER.comment("BBL Ability Lock Client Config");

        BUILDER.push("Client Configs");

        showMillisecondsInTimer = BUILDER.comment("Show milliseconds in the speedrun timer")
                .define("showMillisecondsInTimer", true);

        showAbilityLockWorldCreation = BUILDER.comment(
                        "Show the AbilityLock world creation options - the shortcut button on " +
                                "Select World, and the AbilityLock tab on Create World.")
                .define("Show Ability Lock Screen", true);

        defaultPreset = BUILDER
                .comment("The default preset of the Ability Lock.")
                .define("Default Preset", "abilitylock:season1");

        BUILDER.pop();

        //End
        SPEC = BUILDER.build();
    }
}