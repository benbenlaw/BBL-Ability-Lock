package com.benbenlaw.abilitylock.command;

import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.task.TaskLoader;
import com.benbenlaw.abilitylock.task.TaskManager;
import com.benbenlaw.abilitylock.task.TaskType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AbilityLockCommand {

    private static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    AbilityLoader.DATA.keySet().stream().map(Identifier::toString).toList(), builder);

    private static final SuggestionProvider<CommandSourceStack> TASK_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    TaskLoader.TASKS.keySet().stream().map(Identifier::toString).toList(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("abilitylock")
                        .then(Commands.literal("ability")
                                .then(Commands.literal("unlock")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .executes(AbilityLockCommand::unlockRandom)
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::unlockSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::unlockOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("lock")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::lockSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::lockOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("list")
                                        .executes(AbilityLockCommand::listSelf)
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                                .executes(AbilityLockCommand::listOther)
                                        )
                                )
                                .then(Commands.literal("check")
                                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                        .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                .suggests(ABILITY_SUGGESTIONS)
                                                .executes(AbilityLockCommand::checkAbilitySelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                                        .suggests(ABILITY_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::checkAbilityOther)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("task")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("task", StringArgumentType.greedyString())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::completeTaskSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("task", StringArgumentType.greedyString())
                                                        .suggests(TASK_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::completeTaskOther)
                                                )
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.literal("all")
                                                .executes(AbilityLockCommand::resetAllSelf)
                                                .then(Commands.argument("target", StringArgumentType.word())
                                                        .executes(AbilityLockCommand::resetAllOther)
                                                )
                                        )
                                        .then(Commands.argument("task", StringArgumentType.greedyString())
                                                .suggests(TASK_SUGGESTIONS)
                                                .executes(AbilityLockCommand::resetOneSelf)
                                        )
                                        .then(Commands.argument("target", StringArgumentType.word())
                                                .then(Commands.argument("task", StringArgumentType.greedyString())
                                                        .suggests(TASK_SUGGESTIONS)
                                                        .executes(AbilityLockCommand::resetOneOther)
                                                )
                                        )
                                )
                        )
        );
    }

    private static @Nullable Identifier getAbilityId(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "ability");
        try {
            return Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid ability id: " + raw));
            return null;
        }
    }

    private static @Nullable Identifier getTaskId(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "task");
        try {
            return Identifier.parse(raw);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Invalid task id: " + raw));
            return null;
        }
    }

    public static int unlockRandom(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;

        return AbilityChecker.grantRandomEligible(player)
                .map(id -> {
                    String name = displayNameOf(id);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Unlocked '" + name + "' for " + player.getName().getString()), true);
                    return 1;
                })
                .orElseGet(() -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            player.getName().getString() + " has no eligible abilities left to unlock."), false);
                    return 0;
                });
    }

    private static int unlockSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return resolveAndUnlock(ctx, player);
    }

    private static int unlockOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return resolveAndUnlock(ctx, player);
    }

    private static int lockSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return resolveAndLock(ctx, player);
    }

    private static int lockOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return resolveAndLock(ctx, player);
    }

    private static int resolveAndLock(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (!data.has(abilityId)) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " doesn't have '" + abilityId + "' unlocked."), false);
            return 1;
        }

        return applyLock(ctx, player, abilityId, abilityData);
    }

    private static int applyLock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, Identifier abilityId, AbilityData abilityData) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<Identifier> updated = new HashSet<>(data.unlockedAbilities());
        Set<Identifier> removed = new HashSet<>();
        removeCascade(abilityId, updated, removed);

        AbilityLockData newData = new AbilityLockData(updated, data.presetId(), data.eliminated(), data.lastGranted());
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Locked '" + abilityData.displayName() + "'" +
                        (removed.size() > 1 ? " and " + (removed.size() - 1) + " dependent ability(ies)" : "") +
                        " for " + player.getName().getString()), true);
        return 1;
    }

    private static void removeCascade(Identifier id, Set<Identifier> unlocked, Set<Identifier> removed) {
        if (!unlocked.remove(id)) return;
        removed.add(id);

        for (Map.Entry<Identifier, AbilityData> entry : AbilityLoader.DATA.entrySet()) {
            if (entry.getValue().parents().contains(id)) {
                removeCascade(entry.getKey(), unlocked, removed);
            }
        }
    }

    private static ServerPlayer getSelfOrFail(CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by/as a player."));
            return null;
        }
    }

    private static int resolveAndUnlock(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.has(abilityId)) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " already has '" + abilityId + "' unlocked."), false);
            return 1;
        }

        for (Identifier parent : abilityData.parents()) {
            if (!AbilityChecker.isUnlocked(player, parent)) {
                ctx.getSource().sendFailure(Component.literal(
                        "Cannot unlock '" + abilityId + "' - prerequisite '" + parent + "' is not unlocked yet."));
                return 0;
            }
        }

        AbilityChecker.grantSpecific(player, abilityId);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Unlocked '" + abilityData.displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }

    private static int listSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doList(ctx, player);
    }

    private static int listOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doList(ctx, player);
    }

    private static int doList(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        Set<Identifier> unlocked = data.unlockedAbilities();

        if (unlocked.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    player.getName().getString() + " has not unlocked any abilities yet."), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
                player.getName().getString() + " has unlocked (" + unlocked.size() + "/" + AbilityLoader.DATA.size() + "):"), false);

        unlocked.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .forEach(id -> {
                    String name = displayNameOf(id);
                    ctx.getSource().sendSuccess(() -> Component.literal(" - " + name), false);
                });

        return 1;
    }

    private static String displayNameOf(Identifier id) {
        AbilityData data = AbilityLoader.DATA.get(id);
        return data != null ? data.displayName() : id.toString();
    }

    private static int checkAbilitySelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doCheckAbility(ctx, player);
    }

    private static int checkAbilityOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doCheckAbility(ctx, player);
    }

    private static int doCheckAbility(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier abilityId = getAbilityId(ctx);
        if (abilityId == null) return 0;

        AbilityData abilityData = AbilityLoader.DATA.get(abilityId);
        if (abilityData == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ability: " + abilityId));
            return 0;
        }

        Identifier broken = AbilityChecker.findUnreachableAncestor(player, abilityId);

        if (broken == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "'" + abilityData.displayName() + "' (" + abilityId + ") CAN be unlocked during "
                            + player.getName().getString() + "'s current run."), false);
            return 1;
        }

        AbilityData brokenData = AbilityLoader.DATA.get(broken);
        String reason = brokenData == null
                ? "'" + broken + "' does not exist (broken ability id)"
                : "'" + brokenData.displayName() + "' (" + broken + ") is excluded by the active preset";

        String location = broken.equals(abilityId) ? "" : " (blocked via ancestor)";

        ctx.getSource().sendFailure(Component.literal(
                "'" + abilityData.displayName() + "' (" + abilityId + ") CANNOT be unlocked during "
                        + player.getName().getString() + "'s current run" + location + ": " + reason));
        return 0;
    }

    private static int completeTaskSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doCompleteTask(ctx, player);
    }

    private static int completeTaskOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doCompleteTask(ctx, player);
    }

    private static int doCompleteTask(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier taskId = getTaskId(ctx);
        if (taskId == null) return 0;

        TaskType task = TaskLoader.TASKS.get(taskId);
        if (task == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        TaskManager.forceComplete(player, task);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Marked '" + task.getData().displayName() + "' complete for " + player.getName().getString()), true);
        return 1;
    }

    private static int resetAllSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doResetAll(ctx, player);
    }

    private static int resetAllOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doResetAll(ctx, player);
    }

    private static int doResetAll(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        TaskManager.resetAll(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset all task progress for " + player.getName().getString()), true);
        return 1;
    }

    private static int resetOneSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = getSelfOrFail(ctx);
        if (player == null) return 0;
        return doResetOne(ctx, player);
    }

    private static int resetOneOther(CommandContext<CommandSourceStack> ctx) {
        String targetName = StringArgumentType.getString(ctx, "target");
        ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayerByName(targetName);
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Player '" + targetName + "' not found or not online."));
            return 0;
        }
        return doResetOne(ctx, player);
    }

    private static int doResetOne(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        Identifier taskId = getTaskId(ctx);
        if (taskId == null) return 0;

        TaskType task = TaskLoader.TASKS.get(taskId);
        if (task == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown task: " + taskId));
            return 0;
        }

        TaskManager.resetOne(player, taskId);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset task '" + task.getData().displayName() + "' for " + player.getName().getString()), true);
        return 1;
    }
}