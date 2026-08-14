package com.benbenlaw.abilitylock.ability.old;

import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import com.benbenlaw.abilitylock.attachment.TaskProgressData;
import com.benbenlaw.abilitylock.network.packet.SyncAbilityLockPacket;
import com.benbenlaw.abilitylock.task.Task;
import com.benbenlaw.abilitylock.task.TaskRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AbilityChecker {

    public static boolean isUnlocked(Player player, String abilityId) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        return isUnlockedRecursive(data, abilityId);
    }

    private static boolean isUnlockedRecursive(AbilityLockData data, String abilityId) {
        if (!data.has(abilityId)) return false;

        Optional<Ability> ability = AbilityRegistry.get(abilityId);
        if (ability.isEmpty()) return false;

        Ability a = ability.get();
        if (!a.hasParent()) return true;

        return isUnlockedRecursive(data, a.parent());
    }

    public static boolean hasAll(Player player, Collection<String> abilityIds) {
        for (String id : abilityIds) {
            if (!isUnlocked(player, id)) return false;
        }
        return true;
    }

    public static boolean canDamage(Player player, EntityType<?> entityType) {
        return hasAll(player, MobAbilityRestrictions.requiredAbilities(entityType));
    }

    public static boolean canCraft(Player player, String recipeId) {
        return hasAll(player, RecipeAbilityRestrictions.requiredAbilities(recipeId));
    }

    public static List<Ability> getEligibleAbilities(AbilityLockData data) {
        List<Ability> eligible = new ArrayList<>();
        for (Ability ability : AbilityRegistry.all().values()) {
            if (data.has(ability.id())) continue;
            if (ability.hasParent() && !data.has(ability.parent())) continue;
            eligible.add(ability);
        }
        return eligible;
    }

    public static Optional<Ability> grantRandomEligible(ServerPlayer player) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        List<Ability> eligible = getEligibleAbilities(data);
        if (eligible.isEmpty()) return Optional.empty();

        Set<String> relevant = relevantAbilitiesForPlayer(player);
        List<Ability> preferred = new ArrayList<>();
        for (Ability ability : eligible) {
            if (relevant.contains(ability.id())) preferred.add(ability);
        }

        List<Ability> pool = preferred.isEmpty() ? eligible : preferred;
        Ability chosen = pool.get(player.getRandom().nextInt(pool.size()));
        grant(player, data, chosen);
        return Optional.of(chosen);
    }

    private static Set<String> relevantAbilitiesForPlayer(ServerPlayer player) {
        TaskProgressData taskData = player.getData(AbilityLockAttachments.TASK_PROGRESS);
        Set<String> relevant = new HashSet<>();

        for (String taskId : taskData.gridTaskIds()) {
            if (taskData.isComplete(taskId)) continue;

            Optional<Task> task = TaskRegistry.get(taskId);
            if (task.isEmpty()) continue;

            for (String abilityId : task.get().requiredAbilities()) {
                relevant.addAll(AbilityRegistry.withAncestors(abilityId));
            }
        }

        return relevant;
    }

    public static void grantSpecific(ServerPlayer player, Ability ability) {
        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);
        grant(player, data, ability);
    }

    private static void grant(ServerPlayer player, AbilityLockData data, Ability ability) {
        Set<String> updated = new HashSet<>(data.unlockedAbilities());
        updated.add(ability.id());
        AbilityLockData newData = new AbilityLockData(updated);
        player.setData(AbilityLockAttachments.ABILITY_LOCK, newData);
        PacketDistributor.sendToPlayer(player, new SyncAbilityLockPacket(newData));
    }
}