package com.benbenlaw.abilitylock.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> showMillisecondsInTimer;

    static {
        BUILDER.comment("BBL Ability Lock Client Config");

        BUILDER.push("Client Configs");

        showMillisecondsInTimer = BUILDER.comment("Show milliseconds in the speedrun timer")
                .define("showMillisecondsInTimer", true);

        BUILDER.pop();


        //End
        SPEC = BUILDER.build();
    }
}
