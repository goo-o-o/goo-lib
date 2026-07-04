package com.goo.goo_lib.client.events;

import com.goo.goo_lib.client.particle.ComponentParticle;
import com.goo.goo_lib.client.particle.gui.GuiParticleSystem;
import com.goo.goo_lib.client.registry.GLParticles;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        GuiParticleSystem.getInstance().tick();
    }

    @SubscribeEvent
    public static void onRenderScreen(ScreenEvent.Render.Post event) {
        // design choice because the game's timer is paused here (rendering will be choppy)
        if (event.getScreen().isPauseScreen()) return;

        GuiParticleSystem.getInstance().render(event.getGuiGraphics(),
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(GLParticles.COMPONENT_PARTICLE.get(), new ComponentParticle.Provider());
    }


}
