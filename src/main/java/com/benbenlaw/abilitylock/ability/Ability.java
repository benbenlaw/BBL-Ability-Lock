package com.benbenlaw.abilitylock.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.List;

public abstract class Ability {

    private Identifier id;
    private AbilityData data;

    public void onBlockBreak(BreakBlockEvent event, AbilityData data) {}
    public void onEntityDamage(LivingDamageEvent.Pre event, AbilityData data) {}
    public void onDimensionTravel(EntityTravelToDimensionEvent event, Player player, ResourceKey<Level> targetDimension, AbilityData data) {}
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event, AbilityData data) {}

    public void setId(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public void setData(AbilityData data) {
        this.data = data;
    }

    public AbilityData getData() {
        return data;
    }

    public Component getDisplayName() {
        return data != null ? Component.translatable(data.displayName()) : Component.literal(id.toString());
    }

    public List<Identifier> getParents() {
        return data != null ? data.parents() : List.of();
    }

    public List<String> getTargets() {
        return data != null ? data.targets() : List.of();
    }
}