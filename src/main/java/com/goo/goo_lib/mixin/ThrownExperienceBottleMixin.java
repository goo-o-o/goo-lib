package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownExperienceBottle.class)
public class ThrownExperienceBottleMixin {

    @Redirect(
            method = "onHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"
            )
    )
    private void onAwardXp(ServerLevel level, Vec3 pos, int originalXpValue, HitResult result) {
        ThrownExperienceBottle bottle = (ThrownExperienceBottle) (Object) this;

        Entity owner = bottle.getOwner();
        int finalXpValue = originalXpValue;

        if (owner instanceof Player player) {
            finalXpValue = (int) (originalXpValue * player.getAttributeValue(GLAttributes.XP_GAIN));
        }

        ExperienceOrb.award(level, pos, finalXpValue);
    }
}