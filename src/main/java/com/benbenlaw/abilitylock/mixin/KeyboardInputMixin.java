package com.benbenlaw.abilitylock.mixin;

import com.benbenlaw.abilitylock.ability.Abilities;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void abilitylock$restrictInput(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        ClientInputAccessor accessor = (ClientInputAccessor) (Object) this;
        Input keyPresses = accessor.abilitylock$getKeyPresses();

        AbilityLockData data = player.getData(AbilityLockAttachments.ABILITY_LOCK);

        boolean forward = keyPresses.forward() && data.has(Abilities.WALK.id());
        boolean backward = keyPresses.backward() && data.has(Abilities.BACKWARDS.id());
        boolean left = keyPresses.left() && data.has(Abilities.STRAFE_LEFT.id());
        boolean right = keyPresses.right() && data.has(Abilities.STRAFE_RIGHT.id());
        boolean jump = keyPresses.jump() && data.has(Abilities.JUMP.id());
        boolean shift = keyPresses.shift() && data.has(Abilities.CROUCH.id());
        boolean sprint = keyPresses.sprint() && data.has(Abilities.SPRINT.id());

        accessor.abilitylock$setKeyPresses(new Input(forward, backward, left, right, jump, shift, sprint));

        float forwardImpulse = calculateImpulse(forward, backward);
        float leftImpulse = calculateImpulse(left, right);
        accessor.abilitylock$setMoveVector(new Vec2(leftImpulse, forwardImpulse).normalized());
    }

    private static float calculateImpulse(boolean positive, boolean negative) {
        if (positive == negative) return 0.0F;
        return positive ? 1.0F : -1.0F;
    }
}