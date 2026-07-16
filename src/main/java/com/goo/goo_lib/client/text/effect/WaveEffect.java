package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.joml.Math;
import org.joml.Matrix4f;

public class WaveEffect implements TextEffect<WaveEffect.Config> {

    public record Config(float speed, float amplitude, float frequency) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(Config::speed),
                Codec.FLOAT.optionalFieldOf("amplitude", 1.0F).forGetter(Config::amplitude),
                Codec.FLOAT.optionalFieldOf("frequency", 0.25F).forGetter(Config::frequency)
        ).apply(inst, Config::new));
    }

    @Override
    public void applyEffect(GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        float time = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0)
                + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        // Pulling wave characteristics smoothly from the data block parameters
        float waveOffset = pX * config.frequency();
        float waveY = Math.sin(time * config.speed() + waveOffset) * config.amplitude();

        for (int i = 0; i < 4; i++) {
            vertexData.positions[i].y += waveY;
        }
    }

    @Override
    public MapCodec<Config> codec() {
        return Config.CODEC;
    }
}