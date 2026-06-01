package com.goo.goo_lib.mixin;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeRenderType.class)
public interface CompositeRenderTypeAccessor {
    @Accessor("state")
    RenderType.CompositeState getState();
}