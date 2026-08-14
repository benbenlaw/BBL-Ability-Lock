package com.benbenlaw.abilitylock.data.custom;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.ability.AbilityData;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbilityBuilder {

    private final String displayName;
    private final List<Identifier> parents = new ArrayList<>();
    private Identifier type;
    private final List<String> targets = new ArrayList<>();

    private AbilityBuilder(String displayName) {
        this.displayName = displayName;
    }

    public static AbilityBuilder ability(String displayName) {
        return new AbilityBuilder(displayName);
    }

    public AbilityBuilder parent(Identifier parent) {
        this.parents.add(parent);
        return this;
    }

    public AbilityBuilder parent(String path) {
        return parent(AbilityLock.identifier(path));
    }

    public AbilityBuilder type(Identifier type) {
        this.type = type;
        return this;
    }

    public AbilityBuilder target(String target) {
        this.targets.add(target);
        return this;
    }

    public AbilityBuilder targets(String... targets) {
        for (String target : targets) target(target);
        return this;
    }

    public AbilityBuilder targetTag(Identifier tagId) {
        return target("#" + tagId);
    }

    public AbilityData build() {
        return new AbilityData(displayName, List.copyOf(parents), Optional.ofNullable(type), List.copyOf(targets));
    }
}