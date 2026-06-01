package com.goo.goo_lib.utils.text;


import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.utils.text.effect.TextEffect;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public record EffectType<C extends EffectConfig>(MapCodec<C> codec, Function<C, TextEffect<C>> factory) {
    // 1. Define a Registry Key so this acts like a standard Minecraft registry (Items, Blocks, etc.)
    public static final ResourceKey<Registry<EffectType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(GooLib.MOD_ID, "text_effect_types")
    );

    public ConfiguredEffect<C> configure(C config) {
        return new ConfiguredEffect<>(this, factory.apply(config), config);
    }
}