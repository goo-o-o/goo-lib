package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttachments;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {

    @ModifyExpressionValue(
            method = "doKnockback",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyKnockback(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"
            )
    )
    private float scalePunchKnockback(float originalD0) {
        // scale here and at the base hurt knockback
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        return originalD0 * arrow.getData(GLAttachments.ARROW_KNOCKBACK);
    }
}