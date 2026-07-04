package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.text.StyleEffectContainer;
import com.goo.goo_lib.client.text.StyleEffectUtils;
import com.goo.goo_lib.client.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.client.text.effect.base.OverlayEffect;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin({Font.StringRenderOutput.class})
public abstract class StringRenderOutputMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureBufferSource(Font this$0, MultiBufferSource bufferSource, float x, float y, int color, boolean dropShadow, Matrix4f pose, Font.DisplayMode mode, int packedLightCoords, CallbackInfo ci) {
        StyleEffectUtils.CURRENT_BUFFER_SOURCE.set(bufferSource);
    }

    @Inject(method = "finish", at = @At("TAIL"))
    private void clearBufferSource(int backgroundColor, float x, CallbackInfoReturnable<Float> cir) {
        StyleEffectUtils.CURRENT_BUFFER_SOURCE.remove();
        StyleEffectUtils.resetCurrentStyle();
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
        StyleEffectUtils.CURRENT_STYLE.set(style);

        List<ConfiguredEffect<?>> activeEffects = ((StyleEffectContainer) style).gl$getEffects();
        if (activeEffects == null || activeEffects.isEmpty()) return bufferSource.getBuffer(renderType);

        for (ConfiguredEffect<?> configuredEffect : activeEffects) {
            if (configuredEffect.getEffect() instanceof OverlayEffect overlay) {
                RenderType overlayType = overlay.getOverlayRenderType(renderType);
                StyleEffectUtils.CURRENT_BLOOM_TYPE.set(overlayType); // stash for render()
                break;
            }
        }

        return bufferSource.getBuffer(renderType); // always return normal buffer here
    }
}
