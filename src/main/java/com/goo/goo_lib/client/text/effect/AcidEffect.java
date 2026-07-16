package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

public class AcidEffect implements TextEffect<AcidEffect.Config>, OverlayEffect<AcidEffect.Config> {


    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    public record Config(
            float speed,
            float amplitude,            // in pixels
            float noiseScale,           // was "frequency" — size of the melting/bulge regions
            float detailStrength,       // weight of fine secondary noise octave
            float flowStrength,         // weight of the gradient "pull" term
            float wobble,               // UV wobble strength in the fragment shader
            float colorMix,             // blend between original text color and acid color
            float hueSpeed,             // how fast the acid color cycles
            float thicknessScale,       // how much the ribbon's vertical thickness swells/shrinks (0 = off)
            float thicknessFlowSpeed,   // how "wide" the fat/thin regions of the ribbon are, in world space
            float thicknessVariance     // how fast the thickness pattern drifts along the ribbon over time
    ) {
        public static final MapCodec<AcidEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(AcidEffect.Config::speed),
                Codec.FLOAT.optionalFieldOf("amplitude", 2.0F).forGetter(AcidEffect.Config::amplitude),
                Codec.FLOAT.optionalFieldOf("noise_scale", 0.01F).forGetter(AcidEffect.Config::noiseScale),
                Codec.FLOAT.optionalFieldOf("detail_strength", 0.4F).forGetter(AcidEffect.Config::detailStrength),
                Codec.FLOAT.optionalFieldOf("flow_strength", 1.0F).forGetter(AcidEffect.Config::flowStrength),
                Codec.FLOAT.optionalFieldOf("wobble", 1.0F).forGetter(AcidEffect.Config::wobble),
                Codec.FLOAT.optionalFieldOf("color_mix", 0.6F).forGetter(AcidEffect.Config::colorMix),
                Codec.FLOAT.optionalFieldOf("hue_speed", 0.03F).forGetter(AcidEffect.Config::hueSpeed),
                Codec.FLOAT.optionalFieldOf("thickness_scale", 10.0F).forGetter(AcidEffect.Config::thicknessScale),
                Codec.FLOAT.optionalFieldOf("thickness_flow_speed", 0.1F).forGetter(AcidEffect.Config::thicknessFlowSpeed),
                Codec.FLOAT.optionalFieldOf("thickness_variance", 0.25F).forGetter(AcidEffect.Config::thicknessVariance)

        ).apply(inst, AcidEffect.Config::new));
    }

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType, Config config) {
        return null;
    }

    @Override
    public RenderType modifyOriginalRenderType(RenderType sourceType, Config config) {
        ShaderInstance instance = GLRenderTypes.InternalShaders.TEXT_ACID.getInstance();
        if (instance != null) {
            instance.safeGetUniform("Speed").set(config.speed());
            instance.safeGetUniform("Amplitude").set(config.amplitude());
            instance.safeGetUniform("NoiseScale").set(config.noiseScale());
            instance.safeGetUniform("DetailStrength").set(config.detailStrength());
            instance.safeGetUniform("FlowStrength").set(config.flowStrength());
            instance.safeGetUniform("Wobble").set(config.wobble());
            instance.safeGetUniform("ColorMix").set(config.colorMix());
            instance.safeGetUniform("HueSpeed").set(config.hueSpeed());
            instance.safeGetUniform("ThicknessScale").set(config.thicknessScale());
            instance.safeGetUniform("ThicknessFlowSpeed").set(config.thicknessFlowSpeed());
            instance.safeGetUniform("ThicknessVariance").set(config.thicknessVariance());
        }
        return GLRenderTypes.getAcid(sourceType);
    }
}