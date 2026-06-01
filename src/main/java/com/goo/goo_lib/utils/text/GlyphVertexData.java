package com.goo.goo_lib.utils.text;

import org.joml.Vector3f;

public class GlyphVertexData {
    // 4 Corners of a glyph quad: Top-Left, Bottom-Left, Bottom-Right, Top-Right
    public final Vector3f[] positions = new Vector3f[4];
    public final float[] reds = new float[4];
    public final float[] greens = new float[4];
    public final float[] blues = new float[4];
    
    public GlyphVertexData(float left, float right, float top, float bottom, float italicTop, float italicBottom) {
        positions[0] = new Vector3f(left + italicTop, top, 0.0F);      // TL
        positions[1] = new Vector3f(left + italicBottom, bottom, 0.0F); // BL
        positions[2] = new Vector3f(right + italicBottom, bottom, 0.0F);// BR
        positions[3] = new Vector3f(right + italicTop, top, 0.0F);     // TR
    }
    
    public void setCornerColor(int index, float r, float g, float b) {
        this.reds[index] = r;
        this.greens[index] = g;
        this.blues[index] = b;
    }
}