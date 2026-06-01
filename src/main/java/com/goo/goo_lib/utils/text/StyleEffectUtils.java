package com.goo.goo_lib.utils.text;

import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class StyleEffectUtils {


    public static final ThreadLocal<MultiBufferSource> CURRENT_BUFFER_SOURCE = new ThreadLocal<>();
    public static final ThreadLocal<Style> CURRENT_STYLE = ThreadLocal.withInitial(() -> Style.EMPTY);

    public static MutableComponent withEffects(MutableComponent component, List<ConfiguredEffect<?>> effects) {
        Style freshStyle = component.getStyle().withFont(component.getStyle().getFont());
        ((StyleEffectContainer) freshStyle).gl$setEffects(effects);
        // Access the extended Duck Interface directly through the cast
        return component.setStyle(freshStyle);
    }

    public static void resetCurrentStyle() {
        CURRENT_STYLE.remove();
    }


}
