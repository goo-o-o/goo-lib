package com.goo.goo_lib.mixin;

import com.goo.goo_lib.utils.text.StyleEffectContainer;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
}