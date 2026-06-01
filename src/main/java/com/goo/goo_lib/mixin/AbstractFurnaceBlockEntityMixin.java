package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {

    // A thread-safe container to hold our player context temporarily
    @Unique
    private static final ThreadLocal<ServerPlayer> RECENT_PLAYER = new ThreadLocal<>();

    // capture RECENT_PLAYER
    @Inject(method = "awardUsedRecipesAndPopExperience", at = @At("HEAD"))
    private void capturePlayer(ServerPlayer player, CallbackInfo ci) {
        RECENT_PLAYER.set(player);
    }

    @Redirect(
            method = "lambda$getRecipesToAwardAndPopExperience$5",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;createExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;IF)V"
            )
    )
    private static void modifyFurnaceXpWithPlayer(ServerLevel level, Vec3 popVec, int recipeIndex, float experience) {
        ServerPlayer player = RECENT_PLAYER.get();
        float finalXp = experience;

        if (player != null) {
            finalXp = (float) (experience * player.getAttributeBaseValue(GLAttributes.XP_GAIN));
        }

        // creatExperience is private, so let's just mimic logic
        int i = Mth.floor((float) recipeIndex * finalXp);
        float f = Mth.frac((float) recipeIndex * finalXp);
        if (f != 0.0F && Math.random() < (double) f) {
            i++;
        }

        ExperienceOrb.award(level, popVec, i);
    }

    // clean up RECENT_PLAYER
    @Inject(method = "awardUsedRecipesAndPopExperience", at = @At("TAIL"))
    private void clearPlayer(ServerPlayer player, CallbackInfo ci) {
        RECENT_PLAYER.remove();
    }
}