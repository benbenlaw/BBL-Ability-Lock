package com.benbenlaw.abilitylock.mixin;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.ability.abilities.StanceAbility;
import com.benbenlaw.abilitylock.mixin.client.ClientInputAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
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

        boolean forward = keyPresses.forward() && isMovementAllowed(player, "forward");
        boolean backward = keyPresses.backward() && isMovementAllowed(player, "backward");
        boolean left = keyPresses.left() && isMovementAllowed(player, "left");
        boolean right = keyPresses.right() && isMovementAllowed(player, "right");
        boolean jump = keyPresses.jump() && isMovementAllowed(player, "jump");
        boolean shift = keyPresses.shift() && isMovementAllowed(player, "crouch");
        boolean sprintAllowed = isMovementAllowed(player, "sprint");
        boolean sprint = keyPresses.sprint() && sprintAllowed;

        accessor.abilitylock$setKeyPresses(new Input(forward, backward, left, right, jump, shift, sprint));

        float forwardImpulse = calculateImpulse(forward, backward);
        float leftImpulse = calculateImpulse(left, right);
        accessor.abilitylock$setMoveVector(new Vec2(leftImpulse, forwardImpulse).normalized());

        if (!sprintAllowed && player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    private static boolean isMovementAllowed(Player player, String target) {
        for (Ability ability : AbilityLoader.ABILITIES.values()) {
            if (!(ability instanceof StanceAbility)) continue;

            AbilityData data = ability.getData();
            if (data == null || !data.targets().contains(target)) continue;

            if (!AbilityChecker.isUnlocked(player, ability.getId())) return false;
        }
        return true;
    }

    private static float calculateImpulse(boolean positive, boolean negative) {
        if (positive == negative) return 0.0F;
        return positive ? 1.0F : -1.0F;
    }
}