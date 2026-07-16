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

public class ShakeEffect implements TextEffect<ShakeEffect.Config> {

    public record Config(float intensity, float speed) {
        public static final MapCodec<ShakeEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("intensity", 1.0F).forGetter(ShakeEffect.Config::intensity),
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(ShakeEffect.Config::speed)
        ).apply(inst, ShakeEffect.Config::new));
    }


    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        int seed = (int) (Util.getMillis() * 0.001f * config.speed() * 12f + codePoint + index);
        float angle = (seed % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);
        vertexData.shiftCornerPosiions(dirX * config.intensity(), dirY * config.intensity());
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }
}