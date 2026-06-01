package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.registry.GLShaders;
import com.goo.goo_lib.utils.text.StyleEffectContainer;
import com.goo.goo_lib.utils.text.StyleEffectUtils;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.utils.text.effect.base.OverlayEffect;
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
        StyleEffectUtils.CURRENT_STYLE.set(style);
        List<ConfiguredEffect<?>> activeEffects = ((StyleEffectContainer) style).gl$getEffects();

        RenderType specialType = null;
        for (ConfiguredEffect<?> configuredEffect : activeEffects) {
            if (configuredEffect.getEffect() instanceof OverlayEffect overlay) {
                specialType = overlay.getOverlayRenderType(renderType);
                break;
            }
        }

        if (specialType != null) {
            float time = (float)(System.currentTimeMillis() % 10000) / 10000.0f;
            GLShaders.updateGameTime(time);
            return bufferSource.getBuffer(specialType);
        }
        return bufferSource.getBuffer(renderType);
    }
}
