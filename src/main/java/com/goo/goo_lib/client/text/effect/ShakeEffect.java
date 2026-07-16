package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ShakeEffect implements TextEffect<ShakeEffect.Config> {

    public record Config(float intensity, float speed) {
        public static final MapCodec<ShakeEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("intensity", 1.0F).forGetter(ShakeEffect.Config::intensity),
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(ShakeEffect.Config::speed)
        ).apply(inst, ShakeEffect.Config::new));
    }


    // Mirrors the GLSL noise(x) = fract(sin(x) * 43758.5453)
    private static float noise(float x) {
        return (float) (Math.abs(Math.sin(x) * 43758.5453) % 1.0);
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, ShakeEffect.Config config) {
        float speed = config.speed() <= 0 ? 1F : config.speed();
        float intensity = config.intensity() <= 0 ? 1F : config.intensity();

        float time = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0)
                + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        float charId = Mth.floor(pX);
        float scaledTime = time * speed;

        float offsetX = (noise(charId * 10.0F + scaledTime) + 0.5F) * intensity;
        float offsetY = (noise(charId * 10.0F - (scaledTime + 100)) - 0.5F) * intensity;

        for (int i = 0; i < 4; i++) {
            vertexData.positions[i].x += offsetX;
            vertexData.positions[i].y += offsetY;
        }
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }
}