package com.goo.goo_lib.client.text.effect.base;

import com.goo.goo_lib.client.text.effect.config.base.EffectConfig;
import net.minecraft.client.renderer.RenderType;

public interface OverlayEffect {
    /** Returns the render particleType to use for the secondary overlay pass. */
    RenderType getOverlayRenderType(RenderType sourceType);

    /**
     * Alpha multiplier applied to the overlay quad. Override to use config-driven values.
     * The config passed is this effect's own config object.
     */
    default float getOverlayAlpha(EffectConfig config) {
        return 1.0f;
    }

    /** Replaces the original render particleType instead of adding a pass. */
    default RenderType modifyOriginalRenderType(RenderType sourceType) {
        return sourceType;
    }
}