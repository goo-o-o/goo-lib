package com.goo.goo_lib.utils.text.effect.base;

import net.minecraft.client.renderer.RenderType;

public interface OverlayEffect {
    // Renders using a render type
    RenderType getOverlayRenderType(RenderType sourceType);
}