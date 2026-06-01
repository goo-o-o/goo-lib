package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShakeConfig(float intensity, float speed) implements EffectConfig {
    public static final MapCodec<ShakeConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("intensity").forGetter(ShakeConfig::intensity),
            Codec.FLOAT.fieldOf("speed").forGetter(ShakeConfig::speed)
    ).apply(inst, ShakeConfig::new));
}