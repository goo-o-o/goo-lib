package com.goo.goo_lib.utils.text.effect.config;

import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WaveConfig(float speed, float amplitude, float frequency) implements EffectConfig {
    public static final MapCodec<WaveConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("speed").forGetter(WaveConfig::speed),
            Codec.FLOAT.fieldOf("amplitude").forGetter(WaveConfig::amplitude),
            Codec.FLOAT.fieldOf("frequency").forGetter(WaveConfig::frequency)
    ).apply(inst, WaveConfig::new));
}