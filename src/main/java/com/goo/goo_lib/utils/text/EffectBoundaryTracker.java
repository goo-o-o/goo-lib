package com.goo.goo_lib.utils.text;

import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.utils.text.effect.base.QuadEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

public class EffectBoundaryTracker implements FormattedCharSink {
    private final float startY;
    private final Matrix4f matrix;
    private final MultiBufferSource bufferSource;
    private final Font font;

    private float currentX;
    private float quadStartX = -1;

    // PolyMorphic references to maintain state tracking
    private QuadEffect activeEffect = null;

    public EffectBoundaryTracker(float startX, float startY, Matrix4f matrix, MultiBufferSource bufferSource) {
        this.currentX = startX;
        this.startY = startY;
        this.matrix = matrix;
        this.bufferSource = bufferSource;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public boolean accept(int index, @NotNull Style style, int codePoint) {
        float charWidth = this.font.getSplitter().stringWidth(Character.toString(codePoint));

        // 1. Find the first quad effect on the current character
        QuadEffect currentCharacterEffect = null;
        List<ConfiguredEffect<?>> configuredEffects = ((StyleEffectContainer) style).gl$getEffects();

        if (configuredEffects != null) {
            for (ConfiguredEffect<?> configuredEffect : configuredEffects) {
                // Check if your base type or configured instances implement your interface
                if (configuredEffect.getEffect() instanceof QuadEffect quadEffect) {
                    currentCharacterEffect = quadEffect;
                    break;
                }
            }
        }

        // 2. OOP State Machine Logic
        if (currentCharacterEffect != this.activeEffect) {
            // The effect changed! (e.g., from Sparks to None, None to Sparks, or Sparks to Fire)
            // If we were tracking a previous effect, finish it and render it
            if (this.activeEffect != null) {
                this.activeEffect.renderQuad(quadStartX, currentX, startY, matrix, bufferSource);
            }

            // Update state for the new incoming effect type
            if (currentCharacterEffect != null) {
                this.quadStartX = currentX; // Set anchor for new quad bounding box
            }

            this.activeEffect = currentCharacterEffect;
        }

        // Advance cursor position
        currentX += charWidth;
        return true;
    }

    /**
     * Must be called at the end of text.accept() to clean up remaining tail blocks.
     */
    public void renderFinalQuad() {
        if (this.activeEffect != null) {
            this.activeEffect.renderQuad(quadStartX, currentX, startY, matrix, bufferSource);
            this.activeEffect = null;
        }
    }
}