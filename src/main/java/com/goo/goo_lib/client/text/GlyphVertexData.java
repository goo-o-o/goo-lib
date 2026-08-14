package com.goo.goo_lib.client.text;

import org.joml.Vector3f;

public class GlyphVertexData {

    public final Vector3f[] positions = new Vector3f[4]; // top left, bot left, bot right, top right
    public final float[] reds = new float[4];
    public final float[] greens = new float[4];
    public final float[] blues = new float[4];
    public final float[] alphas = new float[4];

    public GlyphVertexData(float left, float right, float top, float bottom, float italicTop, float italicBottom) {
        positions[0] = new Vector3f(left + italicTop, top, 0.0F);      // TL
        positions[1] = new Vector3f(left + italicBottom, bottom, 0.0F); // BL
        positions[2] = new Vector3f(right + italicBottom, bottom, 0.0F);// BR
        positions[3] = new Vector3f(right + italicTop, top, 0.0F);     // TR
    }

    public GlyphVertexData(Vector3f[] positions) {
        System.arraycopy(positions, 0, this.positions, 0, 4);
    }

    public GlyphVertexData copy() {
        Vector3f[] clonedPositions = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            clonedPositions[i] = new Vector3f(this.positions[i]);
        }

        GlyphVertexData copy = new GlyphVertexData(clonedPositions);

        // Clone per-corner colors too, or the copy silently defaults to black
        for (int i = 0; i < 4; i++) {
            copy.setCornerColor(i,
                    this.reds[i],
                    this.greens[i],
                    this.blues[i],
                    this.alphas[i]
            );
            // adjust getter names to whatever GlyphVertexData actually exposes
        }

        return copy;
    }

    public void setAlphas(float a) {
        for (int i = 0; i < 4; i++)
            alphas[i] = a;
    }

    public void mulAlphas(float factor) {
        for (int i = 0; i < 4; i++)
            alphas[i] *= factor;
    }

    public void setReds(float r) {
        for (int i = 0; i < 4; i++)
            reds[i] = r;
    }

    public void mulReds(float factor) {
        for (int i = 0; i < 4; i++)
            reds[i] *= factor;
    }

    public void setGreens(float g) {
        for (int i = 0; i < 4; i++)
            greens[i] = g;
    }

    public void mulGreens(float factor) {
        for (int i = 0; i < 4; i++)
            greens[i] *= factor;
    }

    public void setBlues(float b) {
        for (int i = 0; i < 4; i++)
            blues[i] = b;
    }

    public void mulBLues(float factor) {
        for (int i = 0; i < 4; i++)
            blues[i] *= factor;
    }

    public void setCornerColor(int index, float r, float g, float b, float a) {
        this.reds[index] = r;
        this.greens[index] = g;
        this.blues[index] = b;
        this.alphas[index] = a;
    }

    public void shiftCornerPosiions(float x, float y) {
        for (int i = 0; i < 4; i++) {
            positions[i].x += x;
            positions[i].y += y;
        }
    }

    public void setCornerPosiions(float x, float y) {
        for (int i = 0; i < 4; i++) {
            positions[i].x = x;
            positions[i].y = y;
        }
    }



}