package com.goo.goo_lib.client;

import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.effect.WaveEffect;
import net.minecraft.client.Minecraft;

public class ClientProxy {
    /**
     * From {@link WaveEffect}
     */
    public static void applyWaveEffect(GlyphVertexData vertexData, float pX, WaveEffect.Config config) {
        float time = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0)
                + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        // Pulling wave characteristics smoothly from the data block parameters
        float waveOffset = pX * config.frequency();
        float waveY = (float) (Math.sin(time * config.speed() + waveOffset) * config.amplitude());

        for (int i = 0; i < 4; i++) {
            vertexData.positions[i].y += waveY;
        }
    }
}
