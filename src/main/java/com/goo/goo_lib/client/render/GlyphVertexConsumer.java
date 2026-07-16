package com.goo.goo_lib.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;


/**
 * Allows us to easily store contextual glyph data
 */
public class GlyphVertexConsumer extends VertexConsumerWrapper {
    public float dimFactor;
    public boolean dropShadow;
    public int index, codePoint;
    public Style style;
    public Font font;

    public GlyphVertexConsumer(VertexConsumer parent, Style style, int index, Font font, float dimFactor, boolean dropShadow, int codePoint) {
        super(parent);
        this.index = index;
        this.font = font;
        this.style = style;
        this.dimFactor = dimFactor;
        this.dropShadow = dropShadow;
        this.codePoint = codePoint;
    }
}
