package com.goo.goo_lib.util;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.mixin.StyleAccessorMixin;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;

public class StyleEffectUtil {


    public static Style createStyleWithEffects(Style base, List<ConfiguredEffect<?>> effects) {
        Style fresh = copyOf(base);
        ((StyleEffectContainer) fresh).gl$setEffects(effects);
        return fresh;
    }

    public static MutableComponent withEffects(MutableComponent component, List<ConfiguredEffect<?>> effects) {
        Style fresh = createStyleWithEffects(component.getStyle(), effects);
        return component.setStyle(fresh);
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
