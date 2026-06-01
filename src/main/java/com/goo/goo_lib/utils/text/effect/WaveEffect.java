package com.goo.goo_lib.utils.text.effect;

import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.config.WaveConfig;
import net.minecraft.client.Minecraft;
import org.joml.Math;

public class WaveEffect implements TextEffect<WaveConfig> {
    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, WaveConfig config) {
        float time = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) 
                     + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        
        // Pulling wave characteristics smoothly from the data block parameters
        float waveOffset = pX * config.frequency(); 
        float waveY = Math.sin(time * config.speed() + waveOffset) * config.amplitude();

        for (int i = 0; i < 4; i++) {
            vertexData.positions[i].y += waveY;
        }
    }
}