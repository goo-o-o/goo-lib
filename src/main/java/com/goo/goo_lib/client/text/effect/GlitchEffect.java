package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public class GlitchEffect implements TextEffect<Unit>, OverlayEffect<Unit> {

    @Override
    public @Nullable RenderType getOverlayRenderType(RenderType sourceType, Unit config) {
        return null;
    }

    @Override
    public RenderType modifyOriginalRenderType(RenderType sourceType, Unit config) {
        return GLRenderTypes.getGlitch(sourceType);
    }

    @Override
    public MapCodec<Unit> codec() {
        return MapCodec.unit(Unit.INSTANCE);
    }
}
