package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractSkeleton.class)
public class AbstractSkeletonMixin {

    @ModifyArg(
            method = "performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shoot(DDDFF)V"
            ),
            index = 3
    )
    private float modifyArrowVelocity(float originalVelocity) {
        AbstractSkeleton skeleton = (((AbstractSkeleton) (Object) this));
        return (float) (originalVelocity * skeleton.getAttributeValue(GLAttributes.ARROW_VELOCITY));
    }

}