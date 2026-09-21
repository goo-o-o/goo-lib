package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
                    "applyFormat", "applyFormats"
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

    @ModifyReturnValue(method = "applyTo", at = @At("RETURN"))
    private Style gl$mergeEffectsOnApplyTo(Style result, Style other) {
        if (result != (Object) this) {
            List<ConfiguredEffect<?>> otherEffects = ((StyleEffectContainer) other).gl$getEffects();
            List<ConfiguredEffect<?>> ownEffects = this.gl$getEffects();
            List<ConfiguredEffect<?>> winner = !otherEffects.isEmpty() ? otherEffects : ownEffects;
            if (!winner.isEmpty()) {
                ((StyleEffectContainer) result).gl$setEffects(winner);
            }
        }
        return result;
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    private void gl$equalsIncludesEffects(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // vanilla already says not-equal
        if (!(o instanceof StyleEffectContainer other)) return;
        if (!this.gl$getEffects().equals(other.gl$getEffects())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    private void gl$hashCodeIncludesEffects(CallbackInfoReturnable<Integer> cir) {
        List<ConfiguredEffect<?>> effects = this.gl$getEffects();
        if (!effects.isEmpty()) {
            cir.setReturnValue(cir.getReturnValue() * 31 + effects.hashCode());
        }
    }

}