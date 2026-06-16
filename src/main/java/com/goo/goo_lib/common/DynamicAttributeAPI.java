package com.goo.goo_lib.common;

import com.goo.goo_lib.common.attribute.IDynamicAttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import java.util.ArrayList;
import java.util.List;

public class DynamicAttributeAPI {
    private static final List<IDynamicAttributeModifier> MODIFIERS = new ArrayList<>();
    
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    /**
     * Call this during mod initialization to add a dynamic attribute handler.
     */
    public static void registerModifier(IDynamicAttributeModifier modifier) {
        MODIFIERS.add(modifier);
    }

    /**
     * Internal entrypoint called by the Mixin.
     */
    public static double entryPoint(LivingEntity owner, AttributeInstance instance, double originalValue) {
        if (IS_PROCESSING.get() || MODIFIERS.isEmpty()) {
            return originalValue;
        }

        try {
            IS_PROCESSING.set(true);

            double rollingValue = originalValue;
            for (IDynamicAttributeModifier modifier : MODIFIERS) {
                rollingValue = modifier.modify(owner, instance, rollingValue);
            }

            return rollingValue;
        } finally {
            IS_PROCESSING.set(false);
        }
    }
}