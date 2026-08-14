package com.benbenlaw.abilitylock.ability;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.abilities.BlockBreak;
import com.benbenlaw.abilitylock.ability.abilities.BlockInteract;
import com.benbenlaw.abilitylock.ability.abilities.DimensionTravel;
import com.benbenlaw.abilitylock.ability.abilities.EntityHurt;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AbilityTypes {

    public static final Identifier BLOCK_BREAK = AbilityLock.identifier("block_break");
    public static final Identifier DIMENSION_TRAVEL = AbilityLock.identifier("dimension_travel");
    public static final Identifier BLOCK_INTERACT = AbilityLock.identifier("block_interact");
    public static final Identifier ENTITY_HURT = AbilityLock.identifier("entity_hurt");

    // public static final Identifier ENTITY_DAMAGE = AbilityLock.identifier("entity_damage"); // TODO
    // public static final Identifier CRAFTING = AbilityLock.identifier("crafting"); // TODO

    private static final Map<Identifier, Supplier<Ability>> FACTORIES = new HashMap<>();

    public static void register(Identifier typeId, Supplier<Ability> factory) {
        FACTORIES.put(typeId, factory);
    }

    public static Optional<Ability> create(Identifier typeId) {
        Supplier<Ability> factory = FACTORIES.get(typeId);
        return factory == null ? Optional.empty() : Optional.of(factory.get());
    }

    public static void init() {
        register(BLOCK_BREAK, BlockBreak::new);
        register(DIMENSION_TRAVEL, DimensionTravel::new);
        register(BLOCK_INTERACT, BlockInteract::new);
        register(ENTITY_HURT, EntityHurt::new);
    }
}