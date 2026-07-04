package com.goo.goo_lib.client;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.registry.PostEffectRegistry;
import com.goo.goo_lib.common.GooLib;
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
        PostEffectRegistry.registerEffect(GLRenderTypes.BLUR_SHADER_LOCATION);
    }
}
