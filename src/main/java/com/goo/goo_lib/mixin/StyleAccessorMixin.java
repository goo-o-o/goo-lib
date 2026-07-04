package com.goo.goo_lib.mixin;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Ensures a BRAND NEW INSTANCE is created
 */
@Mixin(Style.class)
public interface StyleAccessorMixin {
    @Accessor("color")
    TextColor gl$color();
    @Accessor("bold") Boolean gl$bold();
    @Accessor("italic") Boolean gl$italic();
    @Accessor("underlined") Boolean gl$underlined();
    @Accessor("strikethrough") Boolean gl$strikethrough();
    @Accessor("obfuscated") Boolean gl$obfuscated();
    @Accessor("clickEvent") ClickEvent gl$clickEvent();
    @Accessor("hoverEvent")
    HoverEvent gl$hoverEvent();
    @Accessor("insertion") String gl$insertion();
    @Accessor("font") ResourceLocation gl$font();

    @Invoker("<init>")
    static Style gl$new(TextColor color, Boolean bold, Boolean italic, Boolean underlined,
                        Boolean strikethrough, Boolean obfuscated, ClickEvent clickEvent,
                        HoverEvent hoverEvent, String insertion, ResourceLocation font) {
        throw new AssertionError();
    }
}