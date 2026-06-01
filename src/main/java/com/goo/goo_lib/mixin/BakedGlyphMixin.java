package com.goo.goo_lib.mixin;

import com.goo.goo_lib.utils.RenderUtils;
import com.goo.goo_lib.utils.text.GlyphVertexData;
import com.goo.goo_lib.utils.text.StyleEffectContainer;
import com.goo.goo_lib.utils.text.StyleEffectUtils;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BakedGlyph.class)
public abstract class BakedGlyphMixin {

    @Shadow
    @Final
    private float left;
    @Shadow
    @Final
    private float right;
    @Shadow
    @Final
    private float up;
    @Shadow
    @Final
    private float down;
    @Shadow
    @Final
    private float u0;
    @Shadow
    @Final
    private float u1;
    @Shadow
    @Final
    private float v0;
    @Shadow
    @Final
    private float v1;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(boolean pItalic, float pX, float pY, Matrix4f pMatrix,
                       VertexConsumer pBuffer, float pRed, float pGreen, float pBlue,
                       float pAlpha, int pPackedLight, CallbackInfo ci) {

        Style currentStyle = StyleEffectUtils.CURRENT_STYLE.get();
        if (currentStyle == null) {
            StyleEffectUtils.resetCurrentStyle();
            return;
        }

        List<ConfiguredEffect<?>> activeEffects = ((StyleEffectContainer) currentStyle).gl$getEffects();
        if (activeEffects == null || activeEffects.isEmpty()) {
            StyleEffectUtils.resetCurrentStyle();
            return;
        }

        ci.cancel();

        // ── Build vertex data ────────────────────────────────────────────────

        float italicTopOffset = pItalic ? 1.0F - 0.25F * this.up : 0.0F;
        float italicBottomOffset = pItalic ? 1.0F - 0.25F * this.down : 0.0F;
        float dimFactor = (pRed == 0.25F && pGreen == 0.25F && pBlue == 0.25F) ? 0.25F : 1.0F;

        GlyphVertexData vertexData = new GlyphVertexData(
                pX + this.left, pX + this.right,
                pY + this.up, pY + this.down,
                italicTopOffset, italicBottomOffset
        );

        for (int i = 0; i < 4; i++) {
            vertexData.setCornerColor(i, pRed, pGreen, pBlue);
        }

        // ── Run effects ──────────────────────────────────────────────────────

        for (ConfiguredEffect<?> configuredEffect : activeEffects) {
            configuredEffect.run(vertexData, pX, pY, dimFactor);
        }
        RenderUtils.writeQuad(pBuffer, pMatrix, vertexData, pAlpha, this.u0, this.v0, this.u1, this.v1, pPackedLight);


        StyleEffectUtils.resetCurrentStyle();
    }
}
