package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.Attributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {

    @ModifyReturnValue(
            method = "getGravity",
            at = @At("RETURN")
    )
    private double modified$modifyGravity(double originalGravity) {
        if ((((Entity) (Object) this)) instanceof AbstractArrow abstractArrow) {
            if (abstractArrow.getOwner() instanceof Player player) {
                return originalGravity * player.getAttributeValue(Attributes.ARROW_GRAVITY);
            }
        }
        return originalGravity;
    }
}