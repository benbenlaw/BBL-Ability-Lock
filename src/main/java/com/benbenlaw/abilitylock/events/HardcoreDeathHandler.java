package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.presets.PresetData;
import com.benbenlaw.abilitylock.presets.PresetLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class HardcoreDeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!shouldGrantAllOnDeath(player)) return;

        grantAllAbilities(player);
    }

    private static boolean shouldGrantAllOnDeath(ServerPlayer player) {
        if (player.level().getLevelData().isHardcore()) return true;

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (data.presetId().isEmpty()) return false;

        PresetData preset = PresetLoader.DATA.get(data.presetId().get());
        return preset != null && preset.onDeathLoseWorld();
    }

    private static void grantAllAbilities(ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        boolean changed = false;
        for (Identifier abilityId : AbilityLoader.DATA.keySet()) {
            if (!data.has(abilityId)) {
                data = data.withUnlocked(abilityId);
                changed = true;
            }
        }

        if (!changed) return;

        player.setData(AbilityLockAttachments.ABILITY_LOCK, data);
        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(data));
    }
}