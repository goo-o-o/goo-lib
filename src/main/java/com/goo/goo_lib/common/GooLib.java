package com.goo.goo_lib.common;


import com.goo.goo_lib.client.registry.GLParticles;
import com.goo.goo_lib.common.registry.GLAttachments;
import com.goo.goo_lib.common.registry.GLAttributes;
import com.goo.goo_lib.common.registry.TextEffects;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;

@Mod(GooLib.MOD_ID)
public class GooLib {
    public static final String MOD_ID = "goo_lib";
    public static final Logger LOGGER = LogUtils.getLogger();


    public GooLib(IEventBus modEventBus, ModContainer modContainer) {
        GLParticles.PARTICLE_TYPES.register(modEventBus);
        GLAttributes.ATTRIBUTES.register(modEventBus);
        GLAttachments.ATTACHMENT_TYPES.register(modEventBus);
        TextEffects.init(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }


    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
