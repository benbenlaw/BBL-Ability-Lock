package com.benbenlaw.abilitylock.mixin;

import com.benbenlaw.abilitylock.ability.Ability;
import com.benbenlaw.abilitylock.ability.AbilityChecker;
import com.benbenlaw.abilitylock.ability.AbilityData;
import com.benbenlaw.abilitylock.ability.AbilityLoader;
import com.benbenlaw.abilitylock.ability.abilities.StanceAbility;
import com.benbenlaw.abilitylock.mixin.client.ClientInputAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    private static final double DOUBLE_JUMP_VELOCITY = 0.42;

    @Unique
    private boolean abilitylock$hasUsedDoubleJump = false;

    @Unique
    private boolean abilitylock$wasJumpPressed = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void abilitylock$restrictInput(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        ClientInputAccessor accessor = (ClientInputAccessor) (Object) this;
        Input keyPresses = accessor.abilitylock$getKeyPresses();

        boolean waterBlocked = player.isInWater() && !isMovementAllowed(player, "swim");

        boolean forward = keyPresses.forward() && isMovementAllowed(player, "forward") && !waterBlocked;
        boolean backward = keyPresses.backward() && isMovementAllowed(player, "backward") && !waterBlocked;
        boolean left = keyPresses.left() && isMovementAllowed(player, "left") && !waterBlocked;
        boolean right = keyPresses.right() && isMovementAllowed(player, "right") && !waterBlocked;
        boolean jump = keyPresses.jump() && isMovementAllowed(player, "jump");
        boolean shift = keyPresses.shift() && isMovementAllowed(player, "crouch");
        boolean sprintAllowed = isMovementAllowed(player, "half_sprint");
        boolean sprint = keyPresses.sprint() && sprintAllowed;

        accessor.abilitylock$setKeyPresses(new Input(forward, backward, left, right, jump, shift, sprint));

        float forwardImpulse = calculateImpulse(forward, backward);
        float leftImpulse = calculateImpulse(left, right);
        accessor.abilitylock$setMoveVector(new Vec2(leftImpulse, forwardImpulse).normalized());

        if (!sprintAllowed && player.isSprinting()) {
            player.setSprinting(false);
        }

        abilitylock$handleDoubleJump(player, jump);
        abilitylock$handleClimb(player);
    }

    @Unique
    private void abilitylock$handleDoubleJump(LocalPlayer player, boolean jump) {
        if (player.onGround()) {
            abilitylock$hasUsedDoubleJump = false;
        }

        boolean jumpJustPressed = jump && !abilitylock$wasJumpPressed;

        if (jumpJustPressed && !player.onGround() && !abilitylock$hasUsedDoubleJump && isMovementAllowed(player, "double_jump")) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x, DOUBLE_JUMP_VELOCITY, velocity.z);
            abilitylock$hasUsedDoubleJump = true;
        }

        abilitylock$wasJumpPressed = jump;
    }

    @Unique
    private void abilitylock$handleClimb(LocalPlayer player) {
        if (!player.onClimbable() || isMovementAllowed(player, "climb")) return;

        Vec3 velocity = player.getDeltaMovement();
        if (velocity.y > 0.0) {
            player.setDeltaMovement(velocity.x, 0.0, velocity.z);
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