package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Redirect(
            method = "retrieve",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"
            )
    )
    private ExperienceOrb onRetrieve(
            Level level, double x, double y, double z, int vanillaXpValue
    ) {
        FishingHook hook = (((FishingHook) (Object) this));
        Player player = hook.getPlayerOwner();
        assert player != null; // we can assert because the original method checks for it
        return new ExperienceOrb(player.level(), x, y, z, (int) (vanillaXpValue * player.getAttributeValue(GLAttributes.XP_GAIN)));
    }
}
