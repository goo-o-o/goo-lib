package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.Attributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @ModifyVariable(
            method = "shoot",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyVelocityWithContext(
            float originalVelocity,
            ServerLevel level,
            LivingEntity shooter,
            InteractionHand hand,
            ItemStack weapon
    ) {
        return (float) (originalVelocity * shooter.getAttributeValue(Attributes.ARROW_VELOCITY));
    }
}
