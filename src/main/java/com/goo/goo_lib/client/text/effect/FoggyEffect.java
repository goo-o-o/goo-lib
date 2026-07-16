package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Unit;

public class FoggyEffect implements TextEffect<Unit>, OverlayEffect<Unit> {


    @Override
    public MapCodec<Unit> codec() {
        return MapCodec.unit(Unit.INSTANCE);
    }

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType, Unit config) {
        return GLRenderTypes.getAbyssal(sourceType);
    }
}
