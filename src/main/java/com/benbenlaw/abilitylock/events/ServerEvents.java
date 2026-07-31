package com.benbenlaw.abilitylock.events;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.command.AbilityLockCommand;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.network.packet.SyncTaskProgressPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = AbilityLock.MOD_ID)
public class ServerEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AbilityLockCommand.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        if (data.unlockedAbilities().isEmpty()) {
            List<Ability> eligible = AbilityChecker.getEligibleAbilities(data);
            if (!eligible.isEmpty()) {
                Ability chosen = eligible.get(player.getRandom().nextInt(eligible.size()));
                data = data.withUnlocked(chosen.id());
                player.setData(AbilityLockAttachments.ABILITY_LOCK, data);
                player.sendSystemMessage(Component.literal("You've unlocked: " + chosen.displayName()));
            }
        }

        TaskProgressData taskData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        PacketDistributor.sendToPlayer(player, new SyncTaskProgressPacket(taskData));

        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(data));
    }
}
