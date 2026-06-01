package com.goo.goo_lib.utils.text.effect.base;

import com.goo.goo_lib.utils.text.EffectType;
import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.TextEffect;
import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class ConfiguredEffect<C extends EffectConfig> {

    // Public entry point stays identical
    public static Codec<ConfiguredEffect<?>> codec(Registry<EffectType<?>> effectTypeRegistry) {
        return createCodecHelper(effectTypeRegistry);
    }

    @SuppressWarnings("unchecked")
    private static <T extends EffectConfig> Codec<ConfiguredEffect<?>> createCodecHelper(Registry<EffectType<?>> registry) {
        return registry.byNameCodec().dispatch(
                // 1. Tell it how to get the style type out of an instance
                ConfiguredEffect::getType,

                // 2. Map directly on the MapCodec returned by type.codec() to avoid fracturing the data layout
                (EffectType<?> type) -> extractMapCodec((EffectType<T>) type)
        );
    }

    // By mapping directly on type.codec() (which is a MapCodec), we remain in a MapCodec context the entire time!
    @SuppressWarnings("unchecked")
    private static <T extends EffectConfig> MapCodec<ConfiguredEffect<?>> extractMapCodec(EffectType<T> type) {
        return type.codec().xmap(
                type::configure,
                (ConfiguredEffect<?> effect) -> (T) effect.config
        );
    }

    private final EffectType<C> type;
    private final TextEffect<C> effect;
    private final C config;

    public ConfiguredEffect(EffectType<C> type, TextEffect<C> effect, C config) {
        this.type = type;
        this.effect = effect;
        this.config = config;
    }

    public EffectType<C> getType() {
        return this.type;
    }

    public void run(GlyphVertexData data, float x, float y, float dim) {
        this.effect.applyEffect(data, x, y, dim, this.config);
    }

    public C getConfig() {
        return config;
    }

    public TextEffect<C> getEffect() {
        return this.effect;
    }
}