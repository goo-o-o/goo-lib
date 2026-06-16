package com.goo.goo_lib.common.attribute;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

@FunctionalInterface
public interface IDynamicAttributeModifier {
    /**
     * Computes a dynamic modification to an attribute on the fly.
     * @param owner The entity that owns the attribute.
     * @param instance The specific attribute instance being evaluated.
     * @param currentValue The value calculated by Minecraft so far.
     * @return The newly modified attribute value.
     */
    double modify(LivingEntity owner, AttributeInstance instance, double currentValue);
}