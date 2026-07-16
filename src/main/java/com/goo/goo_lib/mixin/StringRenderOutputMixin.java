package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.render.GlyphVertexConsumer;
import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.effect.BloomEffect;
import com.goo.goo_lib.client.text.effect.SmoothWaveEffect;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.goo.goo_lib.util.StyleEffectUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin({Font.StringRenderOutput.class})
public abstract class StringRenderOutputMixin {


    @Shadow
    @Final
    private float dimFactor;

    @Shadow
    @Final
    private boolean dropShadow;

    @Shadow
    @Final
    private Font this$0;

    @Inject(method = "finish", at = @At("TAIL"))
    private void clearBufferSource(int backgroundColor, float x, CallbackInfoReturnable<Float> cir) {
        StyleEffectUtil.OVERLAY_PASSES.remove();
    }

    @Redirect(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer onStartRender(MultiBufferSource bufferSource, RenderType renderType,
                                         int index, Style style, int codePoint) {
        if (style == null) return bufferSource.getBuffer(renderType);

        RenderType finalRenderType = renderType;


        List<ConfiguredEffect<?>> activeEffects = ((StyleEffectContainer) style).gl$getEffects();
        if (activeEffects == null || activeEffects.isEmpty()) return bufferSource.getBuffer(renderType);


        SmoothWaveEffect.Config activeWaveConfig = null;
        for (ConfiguredEffect<?> effect : activeEffects) {
            if (effect.getConfig() instanceof SmoothWaveEffect.Config waveConfig) {
                activeWaveConfig = waveConfig;
                break;
            }
        }

        List<StyleEffectUtil.OverlayPass> passes = null;
        for (ConfiguredEffect<?> configuredEffect : activeEffects) {
            if (configuredEffect.getEffect() instanceof OverlayEffect<?> overlay) {
                float alpha = configuredEffect.getOverlayAlpha();
                if (passes == null) passes = new ArrayList<>();

                @SuppressWarnings("unchecked")
                OverlayEffect<Object> rawOverlay = (OverlayEffect<Object>) overlay;

                RenderType overlayType = rawOverlay.getOverlayRenderType(renderType, configuredEffect.getConfig());

                if (configuredEffect.getEffect() instanceof BloomEffect bloom) {
                    bloom.prepareWaveUniforms(activeWaveConfig);
                }

                if (overlayType != null)
                    passes.add(new StyleEffectUtil.OverlayPass(overlayType, alpha));

                finalRenderType = rawOverlay.modifyOriginalRenderType(finalRenderType, configuredEffect.getConfig());

            }
        }
        if (passes != null) {
            StyleEffectUtil.OVERLAY_PASSES.set(passes);
        }


        VertexConsumer targetedBuffer = bufferSource.getBuffer(finalRenderType);
        return new GlyphVertexConsumer(targetedBuffer, style, index, this$0, this.dimFactor, this.dropShadow, codePoint);
    }
}
