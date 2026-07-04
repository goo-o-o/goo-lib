package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    /**
     * The following code was taken from <a href="https://github.com/florensie/ExpandAbility/blob/master/common/src/main/java/be/florens/expandability/mixin/swimming/server/ServerPlayerMixin.java">ExpandAbility</a>. thank you!
     * <ul>
     *     <li>{@link ServerPlayer#checkMovementStatistics(double, double, double)}: makes sure the correct hunger is applied</li>
     * </ul>
     */
    @ModifyExpressionValue(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isInWater()Z"))
    private boolean setInWater(boolean original) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        return PlayerSwimEvent.shouldPlayerSwim(self, original);
    }

    /**
     * Makes sure the correct hunger is applied
     */
    @ModifyExpressionValue(method = "checkMovementStatistics", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean setEyeInFluid(boolean original) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        return PlayerSwimEvent.shouldPlayerSwim(self, original);
    }
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

}