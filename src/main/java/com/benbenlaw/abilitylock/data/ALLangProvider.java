package com.benbenlaw.abilitylock.data;

import com.benbenlaw.abilitylock.AbilityLock;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ALLangProvider extends LanguageProvider {

    public ALLangProvider(PackOutput output) {
        super(output, AbilityLock.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Keys
        add("key.category.abilitylock.abilitylock", "Ability Lock");
        add("key.abilitylock.ability_screen_key", "Open Ability Screen");
        add("key.abilitylock.task_screen_key", "Open Task Screen");

        //Messages
        add("message.abilitylock.cant_break_block", "Cannot break, requires %s ability");
        add("message.abilitylock.cant_travel_to_dimension", "Cannot travel, requires %s ability");
        add("message.abilitylock.cant_interact_with_block", "Cannot interact with this block, requires %s ability");
        add("message.abilitylock.cant_damage_entity", "Cannot deal damage to this mob, requires %s ability");
        add("message.abilitylock.cant_interact_with_entity", "Cannot interact with this entity, requires %s ability");
        add("message.abilitylock.cant_interact_with_item", "Cannot interact with this item, requires %s ability");

    }
}
