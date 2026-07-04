package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    /**
     * Taken from <a href="https://github.com/florensie/ExpandAbility/blob/fluidwalking-rewrite/common/src/main/java/be/florens/expandability/mixin/swimming/client/PlayerRendererMixin.java">ExpandAbility</a>, thank you!
     */
    @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            require = 0 ,// rendering only
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInWater()Z"))
    private boolean setInWater(boolean original, AbstractClientPlayer player) {
        return PlayerSwimEvent.shouldPlayerSwim(player, original);
    }

    @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            require = 0, // rendering only
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInFluidType(Ljava/util/function/BiPredicate;)Z"))
    private boolean setInFluidType(boolean original, AbstractClientPlayer player) {
        return PlayerSwimEvent.shouldPlayerSwim(player, original);
    }
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


}