package com.goo.goo_lib.common.attribute;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class FrictionCalculator {
    public static float handleFriction(LivingEntity livingEntity, float originalFriction) {
        Holder<Attribute> attributeToUse;
        double vanillaSlowdownRate;

        if (livingEntity.onGround()) {
            attributeToUse = GLAttributes.FRICTION_MODIFIER;
            vanillaSlowdownRate = 1.0D - originalFriction;
        } else {
            attributeToUse = GLAttributes.AIR_DRAG_MODIFIER;;
            vanillaSlowdownRate = 0.09;
        }

        AttributeInstance frictionAttribute = livingEntity.getAttribute(attributeToUse);
        if (frictionAttribute == null) return originalFriction;

        double R_new = vanillaSlowdownRate * frictionAttribute.getValue();
        return (float) Mth.clamp(1.0D - R_new, 0.0F, 1.0F);
    }
}