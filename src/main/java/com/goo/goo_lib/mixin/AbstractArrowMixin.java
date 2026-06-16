package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @ModifyArg(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            ),
            index = 1
    )
    private float modified$modifyFinalArrowDamage(float finalDamage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;

        if (arrow.getOwner() instanceof LivingEntity livingEntity) {
            return (float) (finalDamage * livingEntity.getAttributeValue(GLAttributes.ARROW_DAMAGE));
        }

        return finalDamage;
    }
}