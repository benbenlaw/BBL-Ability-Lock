package com.benbenlaw.abilitylock.command;

import com.benbenlaw.abilitylock.screen.AbilityLockScreen;
import com.benbenlaw.abilitylock.screen.TaskScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AbilityLockClientCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("abilitylock")
                .then(Commands.literal("gui")
                    .executes(ctx -> {
                        Minecraft.getInstance().setScreen(new AbilityLockScreen());
                        return 1;
                    })
                )
                    .then(Commands.literal("tasks")
                    .executes(ctx -> {
                        Minecraft.getInstance().setScreen(new TaskScreen());
                        return 1;
                    })
                )
        );
    }
}