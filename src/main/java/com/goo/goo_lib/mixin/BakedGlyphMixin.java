package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.render.GlyphVertexConsumer;
import com.goo.goo_lib.client.text.GlyphVertexData;
import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.TextEffect;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.util.RenderUtil;
import com.goo.goo_lib.util.StyleEffectUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
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
        if (!(pBuffer instanceof GlyphVertexConsumer proxy)) return;


        Style currentStyle = proxy.style;

        List<ConfiguredEffect<?>> activeEffects = ((StyleEffectContainer) currentStyle).gl$getEffects();
        if (activeEffects == null || activeEffects.isEmpty()) return;

        ci.cancel();

        int codePoint = proxy.codePoint;
        int index = proxy.index;
        float dimFactor = proxy.dimFactor;
        boolean isShadow = proxy.dropShadow;
        Font font = proxy.font;

        float italicTopOffset = pItalic ? 1.0F - 0.25F * this.up : 0.0F;
        float italicBottomOffset = pItalic ? 1.0F - 0.25F * this.down : 0.0F;

        GlyphVertexData vertexData = new GlyphVertexData(
                pX + this.left, pX + this.right,
                pY + this.up, pY + this.down,
                italicTopOffset, italicBottomOffset
        );
        for (int i = 0; i < 4; i++) vertexData.setCornerColor(i, pRed, pGreen, pBlue, pAlpha);

        Matrix4f renderMatrix = pMatrix;

        for (ConfiguredEffect<?> effect : activeEffects) {
            effect.run(vertexData, pMatrix, currentStyle, isShadow, index, font, pX, pY, dimFactor, codePoint);
            renderMatrix = effect.applyMatrixTransforms(vertexData, renderMatrix, currentStyle, isShadow, index, font, pX, pY, codePoint);
        }

        List<TextEffect.RenderPass> backgroundPasses = new ArrayList<>();
        for (ConfiguredEffect<?> effect : activeEffects) {
            effect.collectExtraPasses(backgroundPasses, vertexData, renderMatrix, currentStyle, index, font, pX, pY, codePoint);
        }

        for (TextEffect.RenderPass pass : backgroundPasses) {
            GlyphVertexData passData = vertexData.copy();
            int color = pass.color();
            passData.setAlphas(FastColor.ARGB32.alpha(color) / 255F);
            passData.setReds(FastColor.ARGB32.red(color) / 255F);
            passData.setGreens(FastColor.ARGB32.green(color) / 255F);
            passData.setBlues(FastColor.ARGB32.blue(color) / 255F);

            RenderUtil.drawGlyphQuad(pBuffer, pass.matrix(), passData,
                    this.u0, this.v0, this.u1, this.v1, pPackedLight);
        }

        // main quad
        RenderUtil.drawGlyphQuad(pBuffer, renderMatrix, vertexData,
                this.u0, this.v0, this.u1, this.v1, pPackedLight);

        // overlay
        List<StyleEffectUtil.OverlayPass> passes = StyleEffectUtil.OVERLAY_PASSES.get();
        if (passes != null) {
            for (StyleEffectUtil.OverlayPass pass : passes) {
                RenderType overlayType = pass.renderType();
                VertexConsumer overlayBuffer = StyleEffectUtil.TEXT_EFFECT_BUFFER.getBuffer(overlayType);
                RenderUtil.drawGlyphQuad(overlayBuffer, renderMatrix, vertexData,
                        this.u0, this.v0, this.u1, this.v1, pPackedLight);
            }
            StyleEffectUtil.OVERLAY_PASSES.remove();
        }
    }
}
