package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.particle.gui.EmberParticle;
import com.goo.goo_lib.client.particle.gui.GuiParticleSystem;
import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class FireEffect implements TextEffect<FireEffect.Config>, OverlayEffect<FireEffect.Config> {

    @Override
    public @Nullable RenderType getOverlayRenderType(RenderType sourceType, FireEffect.Config config) {
        return null;
    }

    @Override
    public RenderType modifyOriginalRenderType(RenderType sourceType, FireEffect.Config config) {
        return GLRenderTypes.getFlame(sourceType);
    }

    @Override
    public void applyEffect(BakedGlyph glyph, GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        long charHash = Double.doubleToLongBits(pX) ^ Double.doubleToLongBits(pY) ^ (long) index * 31L ^ codePoint;

        long currentTick = System.currentTimeMillis() / 50L; // approx 20 ticks per second
        boolean shouldAttemptSpawn = ((currentTick + Math.abs(charHash)) % 4L) == 0L;

        if (shouldAttemptSpawn && random.nextFloat() <= config.chance()) {
            Vector3f[] pos = vertexData.positions;

            float randomX = random.nextFloat(pos[0].x, pos[3].x);
            float randomY = random.nextFloat(pos[0].y, pos[1].y);

            EmberParticle particle = new EmberParticle(
                    randomX,
                    randomY,
                    401,
                    0,
                    random.nextFloat(-1.25F, -0.75F),
                    20,
                    random.nextFloat(1.25F, 2.5F)
            );
            GuiParticleSystem.getInstance().add(particle);
        }
    }

    @Override
    public MapCodec<FireEffect.Config> codec() {
        return Config.CODEC;
    }

    @Builder
    public record Config(float chance) {
        public static final MapCodec<FireEffect.Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("chance", 0.05F).forGetter(FireEffect.Config::chance) // chance per tick per char
        ).apply(inst, FireEffect.Config::new));
    }
}
