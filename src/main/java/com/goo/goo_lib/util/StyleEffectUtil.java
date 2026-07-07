package com.goo.goo_lib.util;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.mixin.StyleAccessorMixin;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class StyleEffectUtil {

    /**
     * Shared secondary-render buffer for all text overlay effects.
     * Glyph mixins write overlay quads here; it is flushed by the GUI pipeline before post-processing.
     */
    public static final MultiBufferSource.BufferSource TEXT_EFFECT_BUFFER = MultiBufferSource.immediate(new ByteBufferBuilder(1024));

    /**
     * One entry per overlay effect active for the current glyph.
     * Set by StringRenderOutputMixin, consumed and cleared by BakedGlyphMixin.
     */
    public static final ThreadLocal<List<OverlayPass>> OVERLAY_PASSES = new ThreadLocal<>();

    public static final ThreadLocal<Style> CURRENT_STYLE = ThreadLocal.withInitial(() -> Style.EMPTY);

    public record OverlayPass(RenderType renderType, float alpha) {}

    public static Style createStyleWithEffects(Style base, List<ConfiguredEffect<?>> effects) {
        Style fresh = copyOf(base);
        ((StyleEffectContainer) fresh).gl$setEffects(effects);
        return fresh;
    }

    public static MutableComponent withEffects(MutableComponent component, List<ConfiguredEffect<?>> effects) {
        Style fresh = createStyleWithEffects(component.getStyle(), effects);
        return component.setStyle(fresh);
    }

    public static void resetCurrentStyle() {
        CURRENT_STYLE.remove();
        OVERLAY_PASSES.remove();
    }

    public static Style copyOf(Style base) {
        StyleAccessorMixin a = (StyleAccessorMixin) base;
        return StyleAccessorMixin.gl$new(
                a.gl$color(), a.gl$bold(), a.gl$italic(), a.gl$underlined(),
                a.gl$strikethrough(), a.gl$obfuscated(), a.gl$clickEvent(),
                a.gl$hoverEvent(), a.gl$insertion(), a.gl$font()
        );
    }
}
