package com.goo.goo_lib.client.render;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface for providing outline colors.
 * Return the desired color each tick, the system handles smooth interpolation.
 */
@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface OutlineColorProvider {
    
    /**
     * Get the target color for this item.
     * Called every tick. The system will smoothly lerp towards this color.
     * 
     * @param stack  the item stack
     * @param entity the entity holding the item, or null
     * @return the ARGB color to lerp towards, or 0 to fade out
     */
    int getColor(ItemStack stack, @Nullable LivingEntity entity, float partialTick);
}