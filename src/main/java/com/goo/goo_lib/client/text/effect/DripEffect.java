
package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.particle.gui.DripGuiParticle;
import com.goo.goo_lib.client.particle.gui.GuiParticleSystem;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.util.GLCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class DripEffect implements TextEffect<DripEffect.Config> {

    @Override
    public void applyEffect(BakedGlyph glyph, GlyphVertexData vertexData, Matrix4f matrix, Style style, boolean dropShadow, int index, Font font, float pX, float pY, float dimFactor, int codePoint, Config config) {
        if (System.currentTimeMillis() % 20 == 0) {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            if (random.nextFloat() <= config.chance()) {
                Vector3f[] pos = vertexData.positions;

                float randomX = random.nextFloat(pos[0].x, pos[3].x);
                float randomY = random.nextFloat(pos[0].y, pos[1].y);

                int color = config.color();
                float r = FastColor.ARGB32.red(color);
                float g = FastColor.ARGB32.green(color);
                float b = FastColor.ARGB32.blue(color);
                float a = FastColor.ARGB32.alpha(color);

                DripGuiParticle particle = new DripGuiParticle(color, randomX, randomY, 401, 0, 0, 60, 1, r, g, b, a);
                particle.withGravity(config.gravity());
                GuiParticleSystem.getInstance().add(particle);
            }
        }
    }

    public MapCodec<Config> codec() {
        return Config.CODEC;
    }

    @Builder
    public record Config(Integer color, float chance, float gravity) {
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                GLCodecs.UNIVERSAL_COLOR_CODEC.optionalFieldOf("color", FastColor.ARGB32.color(255, 255, 255, 255)).forGetter(Config::color),
                Codec.FLOAT.optionalFieldOf("chance", 0.025F).forGetter(Config::chance), // chance per tick per char
                Codec.FLOAT.optionalFieldOf("gravity", 1F).forGetter(Config::gravity)
        ).apply(inst, Config::new));
    }
}
