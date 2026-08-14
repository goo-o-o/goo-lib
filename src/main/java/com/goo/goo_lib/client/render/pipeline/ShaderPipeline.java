package com.goo.goo_lib.client.render.pipeline;

import com.goo.goo_lib.client.render.PostEffectRegistry;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public abstract class ShaderPipeline {

    public abstract ResourceLocation getLocation();

    public abstract PipelineStage getStage();

    public boolean isDisabled() {
        return false;
    }

    public BlitMode getBlitMode() {
        return BlitMode.TRANSLUCENT;
    }

    public enum PipelineStage {
        GUI, WORLD, SCREEN;

        public String suffix() {
            return "_" + this.name().toLowerCase(Locale.ROOT);
        }

    }

    public enum BlitMode {
        ADDITIVE, TRANSLUCENT, OPAQUE, DEPTH_AWARE_TRANSLUCENT
    }


    public void onBeforeProcess(PostEffectRegistry.PostEffect postEffect, RenderTarget mainTarget) {}

    public void onAfterProcess(PostEffectRegistry.PostEffect postEffect, RenderTarget mainTarget) {}

    public void onBeforeBlit(PostEffectRegistry.PostEffect postEffect) {}

    public void onAfterBlit(PostEffectRegistry.PostEffect postEffect) {}
}
