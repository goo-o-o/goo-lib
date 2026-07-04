package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallLocation.class)
public abstract class FallLocationMixin {

    /**
     * Taken from <a href="https://github.com/florensie/ExpandAbility/blob/fd1fb9c74121dde961d2fc5c621ac85b5420380e/common/src/main/java/be/florens/expandability/mixin/swimming/FallLocationMixin.java">ExpandAbility</a>, thank you!
     */
    // TODO: set mixin.debug.countInjections to true in runs using build.gradle (expect = 1)
    // Require 0 because this is extremely unimportant, mixin is allowed to fail
    @ModifyExpressionValue(method = "getCurrentFallLocation", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private static boolean setInWater(boolean original, LivingEntity entity) {
        if (entity instanceof Player player)
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        return original;
    }
}