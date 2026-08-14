package com.goo.goo_lib.mixin;

import com.goo.goo_lib.util.color.EnvironmentColorType;
import com.goo.goo_lib.util.color.EnvironmentColorUtil;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Biome.class})
public class BiomeMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void modifySkyColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.SKY);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void modifyFogColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.FOG);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.GRASS);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.FOLIAGE);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "getWaterColor", at = @At("RETURN"), cancellable = true)
    private void modifyWaterColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.WATER);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

    @Inject(method = "getWaterFogColor", at = @At("RETURN"), cancellable = true)
    private void modifyWaterFogColor(CallbackInfoReturnable<Integer> cir) {
        if (FMLEnvironment.dist.isClient()) {
            Integer override = EnvironmentColorUtil.getOverride(EnvironmentColorType.WATER_FOG);
            if (override != null)
                cir.setReturnValue(override);
        }
    }

}
