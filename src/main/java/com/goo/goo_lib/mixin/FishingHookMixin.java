package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Shadow
    @Nullable
    public abstract Player getPlayerOwner();

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

    @WrapOperation(
            method = "retrieve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withLuck(F)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"
            )
    )
    private LootParams.Builder modifyFishingLuck(
            LootParams.Builder builder, float originalLuck, Operation<LootParams.Builder> original
    ) {
        Player player = this.getPlayerOwner();

        if (player != null) {
            return original.call(builder, (float) (originalLuck + player.getAttributeValue(GLAttributes.FISHING_LUCK)));
        }

        return original.call(builder, originalLuck);
    }


    /**
     * Update {@link Player#fishing} to the next fishing hook if present, this won't happen most of the time if reeling all the hooks at once but is good to have
     */
    @WrapMethod(method = "remove")
    private void updateOwnerFishingReferenceOnRemove(Entity.RemovalReason reason, Operation<Void> original) {
        FishingHook hook = (FishingHook) (Object) this;
        Player owner = hook.getPlayerOwner();

        original.call(reason);

        if (owner != null && owner.fishing == hook) {
            // all hooks are discarded at >= 32 anyway
            AABB searchArea = owner.getBoundingBox().inflate(33);
            List<FishingHook> remaining = hook.level().getEntitiesOfClass(
                    FishingHook.class,
                    searchArea,
                    h -> h != hook && h.getPlayerOwner() == owner && h.isAlive()
            );

            owner.fishing = remaining.isEmpty() ? null : remaining.getFirst();
        }
    }
}
