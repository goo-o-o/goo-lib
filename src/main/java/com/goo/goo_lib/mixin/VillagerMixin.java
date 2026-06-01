package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Villager.class)
public class VillagerMixin {
    @Redirect(method = "rewardTradeXp", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"))
    private ExperienceOrb modifyTradeXp(Level level, double x, double y, double z, int value) {
        Villager trader = (((Villager) (Object) this));
        if (trader.getTradingPlayer() != null) {
            return new ExperienceOrb(level, x, y, z, (int) (value * trader.getTradingPlayer().getAttributeValue(GLAttributes.XP_GAIN)));
        }
        return new ExperienceOrb(level, x, y, z, value);
    }
}
