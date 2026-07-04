package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.List;

@Mixin(Style.class)
public class StyleMixin implements StyleEffectContainer {

    @Unique
    private List<ConfiguredEffect<?>> gl$effects = Collections.emptyList();

    @Override
    public List<ConfiguredEffect<?>> gl$getEffects() {
        return this.gl$effects;
    }

    @Override
    public void gl$setEffects(List<ConfiguredEffect<?>> effects) {
        this.gl$effects = List.copyOf(effects);
    }

    @ModifyReturnValue(
            method = {
                    "withColor*",
                    "withBold", "withItalic", "withUnderlined", "withStrikethrough", "withObfuscated",
                    "withClickEvent", "withHoverEvent", "withInsertion", "withFont",
                    "applyFormat", "applyFormats", "applyTo"
            },
            at = @At("RETURN")
    )
    private Style gl$propagateEffects(Style result) {
        if (result != (Object) this) {
            List<ConfiguredEffect<?>> effects = this.gl$getEffects();
            if (!effects.isEmpty()) {
                ((StyleEffectContainer) result).gl$setEffects(effects);
            }
        }
        return result;
    }

}