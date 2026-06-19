package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;

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
    private float modifyArrowDamage(float finalDamage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;

        ItemStack weapon = arrow.getWeaponItem();

        if (weapon != null && !weapon.isEmpty()) {
            ItemAttributeModifiers modifiers = weapon.getAttributeModifiers();

            if (modifiers != null) {
                for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                    if (entry.slot() == EquipmentSlotGroup.HAND) {
                        if (entry.attribute().is(Objects.requireNonNull(GLAttributes.ARROW_DAMAGE.getKey()))) {
                            return (float) (finalDamage * entry.modifier().amount());
                        }
                    }
                }
            }
        }

        // fallback
        // return (float) (finalDamage * livingEntity.getAttributeValue(GLAttributes.ARROW_DAMAGE));
        // removed to prevent attribute swapping

        return finalDamage;
    }
}