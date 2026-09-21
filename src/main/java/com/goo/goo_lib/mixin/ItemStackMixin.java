package com.goo.goo_lib.mixin;

import com.goo.goo_lib.util.EffectMarkupParser;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyReturnValue(method = "getHoverName", at = @At("RETURN"))
    private Component onGetHoverName(Component originalName) {
        return EffectMarkupParser.parseCached(originalName.getString());
    }
}