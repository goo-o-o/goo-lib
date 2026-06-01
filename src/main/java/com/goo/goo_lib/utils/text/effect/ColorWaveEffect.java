package com.goo.goo_lib.utils.text.effect;

import com.goo.goo_lib.utils.ColorUtils;
import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.config.GradientConfig;

public class ColorWaveEffect implements TextEffect<GradientConfig> {
    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, GradientConfig config) {
        float leftWorldX = pX + vertexData.positions[0].x;
        float rightWorldX = pX + vertexData.positions[2].x;

        // Pulling dynamic settings straight from the config template
        int colorLeft = ColorUtils.getGradientAt(leftWorldX, config.spread(), config.waveSpeed(), config.colors());
        int colorRight = ColorUtils.getGradientAt(rightWorldX, config.spread(), config.waveSpeed(), config.colors());

        applyRGB(vertexData, 0, colorLeft, dimFactor);
        applyRGB(vertexData, 1, colorLeft, dimFactor);
        applyRGB(vertexData, 2, colorRight, dimFactor);
        applyRGB(vertexData, 3, colorRight, dimFactor);
    }

    private void applyRGB(GlyphVertexData data, int index, int color, float dimFactor) {
        float r = ((color >> 16) & 255) / 255.0F * dimFactor;
        float g = ((color >> 8) & 255) / 255.0F * dimFactor;
        float b = (color & 255) / 255.0F * dimFactor;
        data.setCornerColor(index, r, g, b);
    }
}