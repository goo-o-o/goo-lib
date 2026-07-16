package com.goo.goo_lib.client.text;


import com.goo.goo_lib.client.text.effect.TextEffect;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.common.GooLib;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public record EffectType<C>(MapCodec<C> codec, Function<C, TextEffect<C>> factory) {
    public static final ResourceKey<Registry<EffectType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(GooLib.loc("text_effect_types"));

    public ConfiguredEffect<C> configure(C config) {
        return new ConfiguredEffect<>(this, factory.apply(config), config);
    }
}