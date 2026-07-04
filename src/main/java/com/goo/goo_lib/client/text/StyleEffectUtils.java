package com.goo.goo_lib.client.text;

import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.mixin.StyleAccessorMixin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class StyleEffectUtils {

    public static final ThreadLocal<MultiBufferSource> CURRENT_BUFFER_SOURCE = new ThreadLocal<>();
    public static final ThreadLocal<Style> CURRENT_STYLE = ThreadLocal.withInitial(() -> Style.EMPTY);

    /**
     * This used to use the style passed in, mutating the original while we're at it, now it <b>creates a new instance</b>
     */
    public static Style createStyleWithEffects(Style base, List<ConfiguredEffect<?>> effects) {
        Style fresh = copyOf(base);
        ((StyleEffectContainer) fresh).gl$setEffects(effects);
        return fresh;
    }

    /**
     * Only call this if creating a new Style, not when using a shared style such as Style.EMPTY
     * @param component
     * @param effects
     * @return
     */
    public static MutableComponent withEffects(MutableComponent component, List<ConfiguredEffect<?>> effects) {
        Style fresh = createStyleWithEffects(component.getStyle(), effects);
        return component.setStyle(fresh);
    }

    public static void resetCurrentStyle() {
        CURRENT_STYLE.remove();
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