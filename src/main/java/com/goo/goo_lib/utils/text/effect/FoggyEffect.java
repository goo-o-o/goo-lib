package com.goo.goo_lib.utils.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.base.OverlayEffect;
import com.goo.goo_lib.utils.text.effect.config.FoggyConfig;
import net.minecraft.client.renderer.RenderType;

public class FoggyEffect implements TextEffect<FoggyConfig>, OverlayEffect {
    /**
     * @param vertexData The geometric layout data of the character glyph.
     * @param pX         Original horizontal drawing axis offset.
     * @param pY         Original vertical drawing axis offset.
     * @param dimFactor  Drop shadow intensity modifier.
     * @param config     The custom type-safe configuration object containing parameters.
     */
    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, FoggyConfig config) {

    }

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType) {
        return GLRenderTypes.textAbyssal(sourceType);
    }
}
