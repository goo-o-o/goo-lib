package com.goo.goo_lib.client;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.render.PostEffectRegistry;
import com.goo.goo_lib.client.render.pipeline.GuiShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.MotionBlurPipeline;
import com.goo.goo_lib.client.render.pipeline.ScreenPostEffectPipeline;
import com.goo.goo_lib.client.render.pipeline.WorldShaderPipeline;
import com.goo.goo_lib.common.GooLib;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.

@Mod(value = GooLib.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class GooLibClient {
    public GooLibClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PostEffectRegistry.registerPipeline(new GuiShaderPipeline() {
            @Override
            public ResourceLocation getLocation() {
                return GLRenderTypes.BLOOM_SHADER_LOCATION;
            }

            @Override
            public void flushBuffers() {
                GLRenderTypes.TEXT_EFFECT_BUFFER.endBatch();
            }
        });

        PostEffectRegistry.registerPipeline(new WorldShaderPipeline() {
            @Override
            public ResourceLocation getLocation() {
                return GLRenderTypes.BLUR_SHADER_LOCATION;
            }
        });

        PostEffectRegistry.registerPipeline(new WorldShaderPipeline() {
            @Override
            public ResourceLocation getLocation() {
                return GLRenderTypes.PIXELATE_SHADER_LOCATION;
            }

            @Override
            public BlitMode getBlitMode() {
                return BlitMode.DEPTH_AWARE_TRANSLUCENT;
            }

            @Override
            public void onRenderStart(RenderTarget mainTarget, PostChain chain) {
                // instead of within the render type pixelate output, which can be called multiply times per frame, call it once per frame
                RenderTarget finalTarget = chain.getTempTarget("final"); // clearAndBindWrite already cleared it
                if (finalTarget != null) finalTarget.copyDepthFrom(mainTarget); // opaque scene depth, once
                mainTarget.bindWrite(false);
            }

//            @Override
//            public void onBeforeProcess(PostEffectRegistry.PostEffect postEffect, RenderTarget mainTarget) {
//                mainTarget.copyDepthFrom(postEffect.postChain.getTempTarget("final"));
//            }

            @Override
            public boolean shouldBlitForStage(RenderLevelStageEvent.Stage stage) {
                return stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES;
            }
        });


        PostEffectRegistry.registerPipeline(new ScreenPostEffectPipeline() {
            @Override
            public ResourceLocation getLocation() {
                return GLRenderTypes.PIXELATE_SHADER_LOCATION;
            }

            @Override
            public BlitMode getBlitMode() {
                return BlitMode.TRANSLUCENT;
            }
        });
        PostEffectRegistry.registerPipeline(new WorldShaderPipeline() {
            @Override
            public ResourceLocation getLocation() {
                return GLRenderTypes.BLOOM_SHADER_LOCATION;
            }

            @Override
            public boolean shouldBlitForStage(RenderLevelStageEvent.Stage stage) {
                return stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES;
            }

            @Override
            public boolean shouldProcessForStage(RenderLevelStageEvent.Stage stage) {
                return stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES;
            }

            @Override
            public void onBeforeProcess(PostEffectRegistry.PostEffect postEffect, RenderTarget mainTarget) {
                RenderTarget input = postEffect.postChain.getTempTarget("input");
                if (input != null) {
                    // bind input buffer and flush accumulated bloom quads
                    input.bindWrite(false);
                    GLRenderTypes.BLOOM_BUFFER_SOURCE.endBatch();
                    mainTarget.bindWrite(false);
                }
            }

            @Override
            public void onAfterBlit(PostEffectRegistry.PostEffect postEffect) {
                RenderTarget input = postEffect.postChain.getTempTarget("input");
                if (input != null) {
                    input.clear(Minecraft.ON_OSX);
                }
            }
        });

        PostEffectRegistry.registerPipeline(new MotionBlurPipeline());
    }
}
