package com.goo.goo_lib.client.text.effect.base;

import net.minecraft.client.renderer.RenderType;

public interface OverlayEffect {
    /**
     * Renders over original using another render type
     *
     * @param sourceType
     * @return
     */
    RenderType getOverlayRenderType(RenderType sourceType);

    /**
     * Replaces the original render type
     *
     * @param sourceType
     * @return
     */
    default RenderType modifyOriginalRenderType(RenderType sourceType) {
        return sourceType;
    }
}