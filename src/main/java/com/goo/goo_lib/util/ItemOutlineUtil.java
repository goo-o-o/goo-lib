package com.goo.goo_lib.util;

import com.goo.goo_lib.client.render.OutlineColorProvider;
import com.goo.goo_lib.client.render.OutlineColorRegistry;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOutlineUtil {


    private static LivingEntity pendingLivingEntity = null;

    public static void push(LivingEntity livingEntity) {
        pendingLivingEntity = livingEntity;
    }

    public static void clear() {
        pendingLivingEntity = null;
    }

    /**
     * Get the interpolated color for the current render frame.
     * Uses partialTick from the renderer for smooth sub-tick lerp.
     */
    public static int getCurrentOutlineColor(ItemStack stack, float partialTick) {

        OutlineColorProvider provider = OutlineColorRegistry.getProvider(stack);
        if (provider == null) return 0;


        // Get the current target from the provider

        // Lerp between previous and current target using partial tick
        return provider.getColor(stack, pendingLivingEntity, partialTick);
    }

    /**
     * Get color components as float array for shader uniforms.
     */
    public static float[] getColorComponents(ItemStack stack, float partialTick) {
        int color = getCurrentOutlineColor(stack, partialTick);
        return new float[]{
                FastColor.ARGB32.red(color) / 255F,
                FastColor.ARGB32.green(color) / 255F,
                FastColor.ARGB32.blue(color) / 255F,
                FastColor.ARGB32.alpha(color) / 255F
        };
    }

}