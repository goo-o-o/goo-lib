package com.goo.goo_lib.client.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for outline color providers.
 * Register an item and a color function, the system handles smooth lerping.
 */
@OnlyIn(Dist.CLIENT)
public class OutlineColorRegistry {

    private static final Map<Item, OutlineColorProvider> PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Register a color provider for an item.
     */
    public static void register(Item item, OutlineColorProvider provider) {
        PROVIDERS.put(item, provider);
    }

    /**
     * Register a color provider for a DeferredItem.
     */
    public static void register(DeferredItem<?> item, OutlineColorProvider provider) {
        register(item.get(), provider);
    }

    /**
     * Register a static color for an item.
     */
    public static void register(Item item, int color) {
        register(item, (stack, entity, partialTick) -> color);
    }

    /**
     * Register a static color for a DeferredItem.
     */
    public static void register(DeferredItem<?> item, int color) {
        register(item.get(), color);
    }

    /**
     * Register a color provider for all items of a class.
     */
    public static <T extends Item> void registerForClass(Class<T> clazz, OutlineColorProvider provider) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (clazz.isInstance(item)) {
                register(item, provider);
            }
        }
    }

    /**
     * Get the registered provider for an item.
     */
    public static OutlineColorProvider getProvider(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return PROVIDERS.get(stack.getItem());
    }

    /**
     * Remove a registration.
     */
    public static void unregister(Item item) {
        PROVIDERS.remove(item);
    }

    /**
     * Clear all registrations.
     */
    public static void clear() {
        PROVIDERS.clear();
    }
}