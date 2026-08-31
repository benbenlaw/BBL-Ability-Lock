package com.benbenlaw.abilitylock.ability;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.abilities.*;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AbilityTypes {

    public static final Identifier BLOCK_BREAK = AbilityLock.identifier("block_break_ability");
    public static final Identifier DIMENSION_TRAVEL = AbilityLock.identifier("dimension_travel_ability");
    public static final Identifier BLOCK_INTERACT = AbilityLock.identifier("block_interact_ability");
    public static final Identifier ENTITY_INTERACT = AbilityLock.identifier("entity_interact_ability");
    public static final Identifier ITEM_INTERACT = AbilityLock.identifier("item_interact_ability");
    public static final Identifier ENTITY_HURT = AbilityLock.identifier("entity_hurt_ability");
    public static final Identifier CRAFTING_ABILITY = AbilityLock.identifier("crafting_ability");
    public static final Identifier MOVEMENT_ABILITY = AbilityLock.identifier("movement_ability");
    public static final Identifier SCREEN_ABILITY = AbilityLock.identifier("screen_ability");

    private static final Map<Identifier, Supplier<Ability>> FACTORIES = new HashMap<>();

    public static void register(Identifier typeId, Supplier<Ability> factory) {
        FACTORIES.put(typeId, factory);
    }

    public static Optional<Ability> create(Identifier typeId) {
        Supplier<Ability> factory = FACTORIES.get(typeId);
        return factory == null ? Optional.empty() : Optional.of(factory.get());
    }

    public static void init() {
        register(BLOCK_BREAK, BlockBreakAbility::new);
        register(DIMENSION_TRAVEL, DimensionTravelAbility::new);
        register(BLOCK_INTERACT, BlockInteractAbility::new);
        register(ENTITY_INTERACT, EntityInteractAbility::new);
        register(ITEM_INTERACT, ItemInteractAbility::new);
        register(ENTITY_HURT, EntityHurtAbility::new);
        register(CRAFTING_ABILITY, CraftingAbility::new);
        register(MOVEMENT_ABILITY, StanceAbility::new);
        register(SCREEN_ABILITY, ScreenOpenAbility::new);
    }
}