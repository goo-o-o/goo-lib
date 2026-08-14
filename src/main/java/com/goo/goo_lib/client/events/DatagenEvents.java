package com.goo.goo_lib.client.events;

import com.goo.goo_lib.client.datagen.GLParticleDescriptionProvider;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class DatagenEvents {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();


        GLParticleDescriptionProvider particleDescriptionProvider = new GLParticleDescriptionProvider(
                packOutput, event.getExistingFileHelper()
        );
        generator.addProvider(event.includeServer(), particleDescriptionProvider);

    }
}
