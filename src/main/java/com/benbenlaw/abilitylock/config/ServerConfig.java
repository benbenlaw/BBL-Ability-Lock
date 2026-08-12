package com.benbenlaw.abilitylock.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue immediateTaskPercentage;

    static {
        BUILDER.comment("BBL Ability Lock Server Config");

        BUILDER.push("Task Grid");

        immediateTaskPercentage = BUILDER
                .comment("Percentage (0-100) of each task grid that must be completable with only the starting abilities.")
                .defineInRange("immediateTaskPercentage", 25, 0, 100);

        BUILDER.pop();


        //End
        SPEC = BUILDER.build();
    }
}