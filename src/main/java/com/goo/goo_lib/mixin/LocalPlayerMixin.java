package com.goo.goo_lib.mixin;


import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    /**
     * Adapted from <a href="https://github.com/florensie/ExpandAbility/blob/6cfae906999c9520e5713241c589c45bb8164a86/common/src/main/java/be/florens/expandability/mixin/swimming/client/LocalPlayerMixin.java">ExpandAbility</a>, thank you!
     */
    @ModifyExpressionValue(method = "aiStep", require = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInWater()Z"))
    private boolean setInWater(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    @ModifyExpressionValue(method = {"aiStep", "hasEnoughImpulseToStartSprinting"}, require = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z"))
    private boolean setUnderWater(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/fluids/FluidType;isAir()Z"))
    private boolean setIsAir(boolean original) {
        if ((Object) this instanceof LocalPlayer player)
            return PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(player), false, true, original);
        return original;
    }

    @ModifyExpressionValue(method = "aiStep", require = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInFluidType(Ljava/util/function/BiPredicate;)Z"))
    private boolean setInFluidType(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    @ModifyExpressionValue(method = "aiStep", require = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;canStartSwimming()Z"))
    private boolean setCanStartSwimming(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;canSwimInFluidType(Lnet/neoforged/neoforge/fluids/FluidType;)Z"))
    private boolean setCanSwimInFluidType(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


}