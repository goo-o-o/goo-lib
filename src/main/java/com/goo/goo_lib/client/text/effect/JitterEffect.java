package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class JitterEffect implements TextEffect<JitterEffect.Config> {

    public record Config(float intensity, float speed) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("intensity", 1F).forGetter(Config::intensity),
                Codec.FLOAT.optionalFieldOf("speed", 1F).forGetter(Config::speed)
        ).apply(inst, Config::new));
    }

    private static float noise(float x) {
        return (float) (Math.abs(Math.sin(x) * 43758.5453) % 1.0);
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {

        float time = Util.getMillis() * 0.001F;

        float charId = Mth.floor(index);
        float scaledTime = time * config.speed();

        float offsetX = (noise(charId * 10.0F + scaledTime) + 0.5F) * config.intensity();
        float offsetY = (noise(charId * 10.0F - (scaledTime + 100)) - 0.5F) * config.intensity();

        vertexData.shiftCornerPosiions(offsetX, offsetY);
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

}