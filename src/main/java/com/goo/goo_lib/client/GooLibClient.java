package com.goo.goo_lib.client;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.registry.PostEffectRegistry;
import com.goo.goo_lib.client.render.MotionBlurPipeline;
import com.goo.goo_lib.client.render.pipeline.EntityShaderPipeline;
import com.goo.goo_lib.client.render.pipeline.GuiShaderPipeline;
import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.StyleEffectUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
            @Override public ResourceLocation getLocation() { return GLRenderTypes.BLOOM_SHADER_LOCATION; }
            @Override public void flushBuffers() { StyleEffectUtil.TEXT_EFFECT_BUFFER.endBatch(); }
        });
        PostEffectRegistry.registerPipeline(new EntityShaderPipeline() {
            @Override public ResourceLocation getLocation() { return GLRenderTypes.BLUR_SHADER_LOCATION; }
        });
        PostEffectRegistry.registerPipeline(new EntityShaderPipeline() {
            @Override public ResourceLocation getLocation() { return GLRenderTypes.BLOOM_SHADER_LOCATION; }
        });
        PostEffectRegistry.registerPipeline(new MotionBlurPipeline());
    }
}
