package com.goo.goo_lib.common;


import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(GooLib.MOD_ID)
public class GooLib {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "goo_lib";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public GooLib(IEventBus modEventBus, ModContainer modContainer) {
        Attributes.ATTRIBUTES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }
}
