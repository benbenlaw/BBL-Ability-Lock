package com.benbenlaw.abilitylock.util;

import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.StopSpeedrunTimerPacket;
import com.benbenlaw.abilitylock.task.Task;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpeedrunManager {

    private static final Map<UUID, String> PENDING_FINAL_TASK = new LinkedHashMap<>();

    public static void onSpeedrunFinished(ServerPlayer player, Task finalTask) {
        PENDING_FINAL_TASK.put(player.getUUID(), finalTask.displayName());
        PacketDistributor.sendToPlayer(player, new StopSpeedrunTimerPacket());
    }

    public static void onTimeReceived(ServerPlayer player, long elapsedMillis) {
        String finalTaskName = PENDING_FINAL_TASK.remove(player.getUUID());
        if (finalTaskName == null) return; // stale or unexpected packet

        String formattedTime = SpeedrunTimeUtil.format(elapsedMillis);
        List<String> unlockedNames = unlockedAbilityNames(player);

        Component summary = Component.literal(
                player.getGameProfile().name() + " finished the speedrun in " + formattedTime
                        + " (Final task: " + finalTaskName + ")");
        player.level().getServer().getPlayerList().broadcastSystemMessage(summary, false);

        Component abilities = Component.literal("Unlocked abilities: " +
                (unlockedNames.isEmpty() ? "None" : String.join(", ", unlockedNames)));
        player.level().getServer().getPlayerList().broadcastSystemMessage(abilities, false);
    }

    private static List<String> unlockedAbilityNames(ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        return AbilityLoader.DATA.entrySet().stream()
                .filter(entry -> data.has(entry.getKey()))
                .map(entry -> entry.getValue().displayName())
                .collect(Collectors.toList());
    }
}