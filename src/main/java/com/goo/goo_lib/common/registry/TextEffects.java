package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.client.text.EffectType;
import com.goo.goo_lib.client.text.effect.*;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.client.text.effect.config.*;
import com.goo.goo_lib.common.GooLib;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class TextEffects {
    public static final Codec<List<ConfiguredEffect<?>>> CONFIGURED_EFFECT_LIST_CODEC =
            Codec.lazyInitialized(() -> ConfiguredEffect.codec(TextEffects.REGISTRY.getRegistry().get()).listOf());

    // Create the root dynamic registry container
    public static final DeferredRegister<EffectType<?>> REGISTRY =
            DeferredRegister.create(EffectType.REGISTRY_KEY, GooLib.MOD_ID);

    // Register our specific configuration parsing modules
    public static final Supplier<EffectType<GradientConfig>> COLOR_GRADIENT_TYPE =
            REGISTRY.register("color_gradient", () -> new EffectType<>(GradientConfig.CODEC, c -> new ColorWaveEffect()));

    public static final Supplier<EffectType<WaveConfig>> WAVE_TYPE =
            REGISTRY.register("wave", () -> new EffectType<>(WaveConfig.CODEC, c -> new WaveEffect()));

    public static final Supplier<EffectType<ShakeConfig>> SHAKE_TYPE =
            REGISTRY.register("shake", () -> new EffectType<>(ShakeConfig.CODEC, c -> new ShakeEffect()));

    public static final Supplier<EffectType<BloomConfig>> BLOOM_TYPE =
            REGISTRY.register("bloom", () -> new EffectType<>(BloomConfig.CODEC, c -> new BloomEffect()));

    public static final Supplier<EffectType<FoggyConfig>> FOGGY_TYPE =
            REGISTRY.register("foggy", () -> new EffectType<>(FoggyConfig.CODEC, c -> new FoggyEffect()));

    public static final Supplier<EffectType<FireConfig>> FIRE_TYPE =
            REGISTRY.register("fire", () -> new EffectType<>(FireConfig.CODEC, c -> new FireEffect()));

    public static void init(IEventBus modBus) {
        // NeoForge passes a consumer configuring a builder; we catch it and do nothing to keep default settings
        REGISTRY.makeRegistry(builder -> {
        });
        REGISTRY.register(modBus);
    }
}