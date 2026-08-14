package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.ClientProxy;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

public class WaveEffect implements TextEffect<WaveEffect.Config> {

    @Builder
    public record Config(float speed, float amplitude, float frequency) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(Config::speed),
                Codec.FLOAT.optionalFieldOf("amplitude", 1.0F).forGetter(Config::amplitude),
                Codec.FLOAT.optionalFieldOf("frequency", 0.25F).forGetter(Config::frequency)
        ).apply(inst, Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        ClientProxy.applyWaveEffect(vertexData, pX, config);
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }
}