package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Unit;

public class FireEffect implements TextEffect<Unit>, OverlayEffect<Unit> {

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType, Unit config) {
        return GLRenderTypes.getFlame(sourceType);
    }


    @Override
    public MapCodec<Unit> codec() {
        return MapCodec.unit(Unit.INSTANCE);
    }
}
