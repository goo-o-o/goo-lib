package com.goo.goo_lib.client.text.effect.base;

import com.goo.goo_lib.client.text.EffectType;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.effect.TextEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class ConfiguredEffect<C> {



    // Public entry point stays identical
    public static Codec<ConfiguredEffect<?>> codec(Registry<EffectType<?>> effectTypeRegistry) {
        return createCodecHelper(effectTypeRegistry);
    }

    // 2. FIX: Change this parameter back from HolderLookup.RegistryLookup to Registry
    @SuppressWarnings("unchecked")
    private static <T> Codec<ConfiguredEffect<?>> createCodecHelper(Registry<EffectType<?>> lookup) {
        return lookup.byNameCodec().dispatch(
                ConfiguredEffect::getType,
                (EffectType<?> type) -> extractMapCodec((EffectType<T>) type)
        );
    }

    // By mapping directly on particleType.codec() (which is a MapCodec), we remain in a MapCodec context the entire time!
    @SuppressWarnings("unchecked")
    private static <T> MapCodec<ConfiguredEffect<?>> extractMapCodec(EffectType<T> type) {
        return type.codec().xmap(
                type::configure,
                (ConfiguredEffect<?> effect) -> (T) effect.getConfig() // lowercase c in config according to shared code
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

    public float getOverlayAlpha() {
        return this.effect instanceof OverlayEffect overlay ? overlay.getOverlayAlpha(this.config) : 1.0f;
    }

    public C getConfig() {
        return config;
    }

    public TextEffect<C> getEffect() {
        return this.effect;
    }
}