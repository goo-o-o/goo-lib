package com.goo.goo_lib.client.text.effect.base;

import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

public interface OverlayEffect<C> {
    /** Returns the render particleType to use for the secondary overlay pass. You may return null if you don't want an overlay */
    @Nullable
    RenderType getOverlayRenderType(RenderType sourceType, C config);

    /**
     * Alpha multiplier applied to the overlay quad. Override to use config-driven values.
     * The config passed is this effect's own config object.
     */
    default float getOverlayAlpha(C config) {
        return 1.0f;
    }

    /** Replaces the original render particleType instead of adding a pass. */
    default RenderType modifyOriginalRenderType(RenderType sourceType, C config) {
        return sourceType;
    }
}