package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsMixin {

    @Redirect(method = "grant", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;giveExperiencePoints(I)V"))
    private void modifyXpReward(ServerPlayer instance, int xpPoints) {
        instance.giveExperiencePoints((int) (instance.getAttributeValue(GLAttributes.XP_GAIN) * xpPoints));
    }
}
