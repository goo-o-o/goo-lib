package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    /**
     * The following is taken from <a href="https://github.com/florensie/ExpandAbility/blob/6cfae906999c9520e5713241c589c45bb8164a86/common/src/main/java/be/florens/expandability/mixin/swimming/PlayerMixin.java">ExpandAbility</a>, thank you!
     * <ul>
     *     <li>{@link Player#attack}: makes it so you can land critical hits while in water with fluid physics disabled</li>
     * </ul>
     */
    @ModifyExpressionValue(method = {"attack", "tryToStartFallFlying"}, require = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWater()Z"))
    private boolean setInWater(boolean original) {
        Player self = (Player) (Object) this;
        return PlayerSwimEvent.shouldPlayerSwim(self, original);
    }

    /**
     * Vanilla checks if the block above the player is fluid and prevents swimming up by look direction
     * This cancels the check if we have swimming enabled
     */
    @ModifyExpressionValue(method = "travel", allow = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;isEmpty()Z"))
    private boolean cancelSurfaceCheck(boolean original) {
        Player self = (Player) (Object) this;
        return PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(self), false, true, original);
    }
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

}