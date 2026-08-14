package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

public class SmoothWaveEffect implements TextEffect<SmoothWaveEffect.Config>, OverlayEffect<SmoothWaveEffect.Config> {
    @Builder
    public record Config(float speed, float amplitude, float frequency) {
        public static final MapCodec<SmoothWaveEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(SmoothWaveEffect.Config::speed),
                Codec.FLOAT.optionalFieldOf("amplitude", 1.0F).forGetter(SmoothWaveEffect.Config::amplitude), // in pixels
                Codec.FLOAT.optionalFieldOf("frequency", 0.15F).forGetter(SmoothWaveEffect.Config::frequency)
        ).apply(inst, SmoothWaveEffect.Config::new));
    }



    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }


    @Override
    public RenderType getOverlayRenderType(RenderType sourceType, Config config) {
        return null;
    }

    @Override
    public RenderType modifyOriginalRenderType(RenderType sourceType, Config config) {
        ShaderInstance instance = GLRenderTypes.InternalShaders.TEXT_SMOOTH_WAVE.getInstance();
        if (instance != null) {
            instance.safeGetUniform("Speed").set(config.speed());
            instance.safeGetUniform("Amplitude").set(config.amplitude());
            instance.safeGetUniform("WaveFrequency").set(config.frequency());
        }
        return GLRenderTypes.getSmoothWave(sourceType);
    }
}
