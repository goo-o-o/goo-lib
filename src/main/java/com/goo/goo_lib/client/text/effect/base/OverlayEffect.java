package com.goo.goo_lib.client.text.effect.base;

import com.goo.goo_lib.client.text.GlyphVertexData;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

public interface OverlayEffect<C> {
    /** Returns the render particleType to use for the secondary overlay pass. You may return null if you don't want an overlay */
    @Nullable
    RenderType getOverlayRenderType(RenderType sourceType, C config);

    /**
     * Called once per overlay pass, on a COPY of the main quad's vertex data,
     * immediately before the overlay quad is drawn. Mutate freely — this never
     * affects the main glyph quad or other overlay passes.
     * Default is a no-op so existing overlay effects that don't need this
     * don't have to implement it.
     */
    default void modifyOverlayVertexData(GlyphVertexData overlayData, C config) {}

    /** Replaces the original render particleType instead of adding a pass. */
    default RenderType modifyOriginalRenderType(RenderType sourceType, C config) {
        return sourceType;
    }
}