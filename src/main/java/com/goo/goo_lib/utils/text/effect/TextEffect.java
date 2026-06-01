package com.goo.goo_lib.utils.text.effect;

import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.effect.config.base.EffectConfig;

public interface TextEffect<C extends EffectConfig> {
    /**
     * @param vertexData The geometric layout data of the character glyph.
     * @param pX Original horizontal drawing axis offset.
     * @param pY Original vertical drawing axis offset.
     * @param dimFactor Drop shadow intensity modifier.
     * @param config The custom type-safe configuration object containing parameters.
     */
    void applyEffect(GlyphVertexData vertexData, float pX, float pY, float dimFactor, C config);
}