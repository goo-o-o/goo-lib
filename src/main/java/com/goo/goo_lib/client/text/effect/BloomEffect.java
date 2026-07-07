package com.goo.goo_lib.client.text.effect;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.registry.PostEffectRegistry;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.goo.goo_lib.client.text.effect.config.BloomConfig;
import com.goo.goo_lib.client.text.effect.config.base.EffectConfig;
import net.minecraft.client.renderer.RenderType;

public class BloomEffect implements TextEffect<BloomConfig>, OverlayEffect {
    /**
     * @param vertexData The geometric layout data of the character glyph.
     * @param pX         Original horizontal drawing axis offset.
     * @param pY         Original vertical drawing axis offset.
     * @param dimFactor  Drop shadow intensity modifier.
     * @param config     The custom type-safe configuration object containing parameters.
     */
    @Override
    public void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, BloomConfig config) {

    }

    @Override
    public RenderType getOverlayRenderType(RenderType sourceType) {
        PostEffectRegistry.renderEffectForNextTick(GLRenderTypes.BLOOM_SHADER_LOCATION);
        return GLRenderTypes.getBloom(sourceType);
    }

    @Override
    public float getOverlayAlpha(EffectConfig config) {
        return config instanceof BloomConfig bc ? bc.intensity() : 0.8f;
    }
}
