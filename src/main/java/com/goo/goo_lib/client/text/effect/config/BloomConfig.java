package com.goo.goo_lib.client.text.effect.config;

import com.goo.goo_lib.client.text.effect.config.base.EffectConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BloomConfig(float intensity) implements EffectConfig {
    public static final MapCodec<BloomConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("intensity", 0.5F).forGetter(BloomConfig::intensity))
            .apply(inst, BloomConfig::new));
}