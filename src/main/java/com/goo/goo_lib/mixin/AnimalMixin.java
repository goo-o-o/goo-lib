package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Animal.class)
public class AnimalMixin {

    @Redirect(
            method = "finalizeSpawnChildFromBreeding",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"
            )
    )
    private ExperienceOrb modifyBreedingXpWithFullContext(
            Level orbLevel, double x, double y, double z, int vanillaXpValue, // Original constructor arguments
            ServerLevel level, Animal parentB, @Nullable AgeableMob baby       // Captured arguments from the method signature
    ) {
        Animal parentA = (Animal) (Object) this;

        ServerPlayer player = parentA.getLoveCause();
        if (player == null) {
            player = parentB.getLoveCause();
        }

        int finalXpValue = vanillaXpValue;

        if (player != null) {
            finalXpValue = (int) (vanillaXpValue * player.getAttributeValue(GLAttributes.XP_GAIN));
        }

        return new ExperienceOrb(orbLevel, x, y, z, finalXpValue);
    }
}