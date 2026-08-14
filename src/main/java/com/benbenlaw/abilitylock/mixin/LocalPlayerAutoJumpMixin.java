package com.benbenlaw.abilitylock.mixin;

import com.benbenlaw.abilitylock.ability.old.Abilities;
import com.benbenlaw.abilitylock.attachment.AbilityLockAttachments;
import com.benbenlaw.abilitylock.attachment.AbilityLockData;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerAutoJumpMixin {

    @Inject(method = "canAutoJump", at = @At("HEAD"), cancellable = true)
    private void abilitylock$blockAutoJumpIfLocked(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer)(Object)this;
        AbilityLockData data = self.getData(AbilityLockAttachments.ABILITY_LOCK);
        if (!data.has(Abilities.JUMP.id())) {
            cir.setReturnValue(false);
        }
    }
}