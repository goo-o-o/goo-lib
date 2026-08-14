package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

public class BloomEffect implements TextEffect<Float>, OverlayEffect<Float> {


    @Override
    public MapCodec<Float> codec() {
        return Codec.FLOAT.optionalFieldOf("intensity", 0.9F);
    }

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType, Float intensity) {
        return GLRenderTypes.getTextBloom(sourceType);
    }

    @Override
    public void modifyOverlayVertexData(GlyphVertexData overlayData, Float intensity) {
        overlayData.setAlphas(intensity);
    }


    public void prepareWaveUniforms(SmoothWaveEffect.Config activeWaveConfig) {
        ShaderInstance instance = GLRenderTypes.InternalShaders.TEXT_BLOOM.getInstance();
        if (instance != null) {
            if (activeWaveConfig != null) {
                instance.safeGetUniform("Speed").set(activeWaveConfig.speed());
                instance.safeGetUniform("Amplitude").set(activeWaveConfig.amplitude());
                instance.safeGetUniform("WaveFrequency").set(activeWaveConfig.frequency());
            } else {
                instance.safeGetUniform("Speed").set(0.0f);
                instance.safeGetUniform("Amplitude").set(0.0f);
                instance.safeGetUniform("WaveFrequency").set(0.0f);
            }
        }
    }
}
