package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.EventResult;
import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.goo.goo_lib.common.registry.GLAttachments;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {


    @ModifyReturnValue(
            method = "getGravity",
            at = @At("RETURN")
    )
    private double modifyGravity(double originalGravity) {
        if ((((Entity) (Object) this)) instanceof AbstractArrow abstractArrow) {
            return originalGravity * abstractArrow.getData(GLAttachments.ARROW_GRAVITY);
        }
        return originalGravity;
    }

    // region FLUID_SWIM ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * The following code was taken and adapted from <a href="https://github.com/florensie/ExpandAbility/blob/fd1fb9c74121dde961d2fc5c621ac85b5420380e/common/src/main/java/be/florens/expandability/mixin/swimming/EntityMixin.java">ExpandAbility</a>, thank you!
     */


    @Shadow
    public abstract boolean isInWater();

    @ModifyExpressionValue(method = {"updateSwimming", "isVisuallyCrawling", "canSpawnSprintParticle", "move", "checkFallDamage" /* GooLib addition */},
            require = 4,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;isInWater()Z"))
    private boolean setInWater(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    /**
     * GooLib addition
     */
    @ModifyExpressionValue(method = {"canSpawnSprintParticle"},
            require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInLava()Z"))
    private boolean spawnSprintParticleInLava(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    /**
     * GooLib addition
     */
    @WrapWithCondition(
            method = "baseTick",
            require = 1,
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/Entity;fallDistance:F"
            )
    )
    private boolean brine$cancelLavaFallDistanceReset(Entity entity, float assignedValue) {
        return PlayerSwimEvent.shouldPlayerSwim(entity, false);
    }

    @ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isUnderWater()Z"))
    private boolean setUnderWater(boolean original) {
        return PlayerSwimEvent.shouldPlayerSwim(this, original);
    }

    /**
     * Prevents the swimming sound from playing when non-vanilla swimming is enabled
     */
    @WrapWithCondition(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;waterSwimSound()V"))
    private boolean cancelPlaySwimSound(Entity entity) {
        // Re-check if we're in water first, so we don't cancel vanilla swimming sounds
        return this.isInWater() || !(entity instanceof Player player && PlayerSwimEvent.postAndGetResult(player) == EventResult.SUCCESS);
    }

    /**
     * Take fall damage when in water with water physics disabled
     */
    @WrapWithCondition(method = {"move"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private boolean cancelResetFallDistance(Entity entity) {
        return !(entity instanceof Player player) || PlayerSwimEvent.postAndGetResult(player) != EventResult.FAIL;
    }

    /**
     * There's a check here for when there's water at the entity's feet. There aren't any vanilla blocks where this
     * matters because honey and soulsand aren't full blocks. There is a block in the fabric testmod to test this
     * behaviour.
     */
    @ModifyExpressionValue(method = "getBlockSpeedFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean fixBlockSpeedFactor(boolean original) {
        //noinspection ConstantConditions
        if ((Object) this instanceof Player player) {
            BlockState block = player.level().getBlockState(player.blockPosition());

            if (block.is(Blocks.WATER) && PlayerSwimEvent.postAndGetResult(player) == EventResult.FAIL) {
                return true; // Makes condition return true
            }
        }

        return original; // Vanilla behaviour
    }

    /**
     * GooLib addition (allows particles from bubble columns)
     */
    @Inject(method = "onAboveBubbleCol",
            at = @At("HEAD"),
            cancellable = true)
    private void cancelOnAboveBubbleColumn(boolean downwards, CallbackInfo ci) {
        if ((((Entity) (Object) this) instanceof Player player)) {
            if (PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(player), false, true, false)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onInsideBubbleColumn",
            at = @At("HEAD"),
            cancellable = true)
    private void cancelOnInsideBubbleColumn(boolean downwards, CallbackInfo ci) {
        if ((((Entity) (Object) this) instanceof Player player)) {
            if (PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(player), false, true, false)) {
                ci.cancel();
            }
        }
    }


    @ModifyExpressionValue(
            method = {"updateSwimming", "isVisuallyCrawling"},
            require = 2,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInFluidType(Ljava/util/function/BiPredicate;)Z"))
    private boolean setInFluidType(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "updateSwimming",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;canStartSwimming()Z"))
    private boolean setCanStartSwimming(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }

    // FIXME: still no fall damage
    @WrapWithCondition(method = "updateInWaterStateAndDoFluidPushing", require = 1, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/Entity;fallDistance:F"))
    private boolean cancelFallDistanceUpdate(Entity entity, float fallDistance) {
        return !(entity instanceof Player player) || PlayerSwimEvent.postAndGetResult(player) != EventResult.FAIL;
    }

    @ModifyExpressionValue(method = "canSpawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInFluidType()Z"))
    private boolean setInFluidTypeNoParams(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }

    /**
     * - $26: Forge 46.x.x
     * - $29: Forge 47.x.x, NeoForge 20.4.x
     * - $28: NeoForge 20.6.x
     * - $22: NeoForge 21.x.x
     */
    @WrapWithCondition(method = {"lambda$updateFluidHeightAndDoFluidPushing$22"},
            require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private boolean shouldDoFluidPushing(Entity entity, Vec3 vec3) {
        return PlayerSwimEvent.shouldPlayerSwim(this, true);
    }

    // endregion ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


}