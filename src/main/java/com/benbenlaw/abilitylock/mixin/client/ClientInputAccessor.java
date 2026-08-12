package com.benbenlaw.abilitylock.mixin.client;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientInput.class)
public interface ClientInputAccessor {

    @Accessor("keyPresses")
    Input abilitylock$getKeyPresses();

    @Accessor("keyPresses")
    void abilitylock$setKeyPresses(Input input);

    @Accessor("moveVector")
    void abilitylock$setMoveVector(Vec2 vec);
}