package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.attribute.IDynamicAttribute;
import com.goo.goo_lib.common.event.custom.EventResult;
import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.goo.goo_lib.common.registry.GLAttributes;
import com.goo.goo_lib.common.attribute.FrictionCalculator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void enchanted$assignOwnerToAttributeMap(EntityType<?> type, Level level, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.getAttributes() instanceof IDynamicAttribute duckMap) {
            duckMap.gl$setOwner(self);
        }
    }

    @Shadow
    protected int useItemRemaining;

    @Redirect(
            method = "updateUsingItem(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I",
                    opcode = 181 // Field Put (assignment / decrement tracking node)
            )
    )
    private void modifySlowDrawDecrement(LivingEntity instance, int assignedValue) {
        double drawSpeed = instance.getAttributeValue(GLAttributes.DRAW_SPEED);

        // Only manipulate this logic if the player has a slow-draw debuff (drawSpeed < 1.0)
        if (drawSpeed < 1.0 && drawSpeed > 0.0) {
            int mod = (int) Math.round(1.0 / drawSpeed); // 0.25 becomes 4

            // If it's NOT the 1-out-of-X tick where time is allowed to move forward...
            if (instance.tickCount % mod != 0) {
                // Cancel the decrement! Maintain the existing timer value exactly as it was.
                // Note: assignedValue would be (useItemRemaining - 1), so we bypass it and keep the original.
                return;
            }
        }

        // Otherwise, proceed with vanilla behavior (standard countdown decrement)
        this.useItemRemaining = assignedValue;
    }


    @Shadow
    protected abstract boolean isAffectedByFluids();
//
//    @Inject(
//            method = "travel",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z",
//                    ordinal = 0
//            ),
//            cancellable = true
//    )
//    private void modifyLavaTravel(Vec3 travelVector, CallbackInfo ci) {
//
//        LivingEntity entity = (LivingEntity) (Object) this;
//
//        if (entity instanceof Player player && PlayerSwimEvent.postAndGetResult(player) == EventResult.FAIL) {
//            return;
//        }
//
//        if (entity.isInLava() && this.isAffectedByFluids()) {
//
//            float efficiency = (float) entity.getAttributeValue(GLAttributes.LAVA_MOVEMENT_EFFICIENCY);
//
//            if (efficiency > 0.0F) {
//                double gravity = this.getGravity();
//                boolean movingDownwards = this.getDeltaMovement().y <= 0.0;
//                if (movingDownwards && entity.hasEffect(MobEffects.SLOW_FALLING)) {
//                    gravity = Math.min(gravity, 0.01);
//                }
//
//                double y = this.getY();
//
//                BlockPos blockBelow = entity.getBlockPosBelowThatAffectsMyMovement();
//                float landFriction = entity.level().getBlockState(blockBelow).getFriction(entity.level(), blockBelow, entity);
//
//
//                float lavaAcceleration = 0.02F;
//                lavaAcceleration += ((float) entity.getAttributeValue(Attributes.MOVEMENT_SPEED) - lavaAcceleration) * efficiency;
//
//                double horizontalDrag = 0.5;
//                horizontalDrag += (landFriction - horizontalDrag) * efficiency;
//
//                this.moveRelative(lavaAcceleration, travelVector);
//                this.move(MoverType.SELF, this.getDeltaMovement());
//
//                if (this.getFluidTypeHeight(NeoForgeMod.LAVA_TYPE.value()) <= this.getFluidJumpThreshold()) {
//                    this.setDeltaMovement(this.getDeltaMovement().multiply(horizontalDrag, 0.8F, horizontalDrag));
//                    Vec3 vec33 = entity.getFluidFallingAdjustedMovement(gravity, movingDownwards, this.getDeltaMovement());
//                    this.setDeltaMovement(vec33);
//                } else {
//                    Vec3 currentMovement = this.getDeltaMovement();
//                    this.setDeltaMovement(currentMovement.x * horizontalDrag, currentMovement.y * 0.5D, currentMovement.z * horizontalDrag);
//                }
//
//                if (gravity != 0.0) {
//                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -gravity / 4.0, 0.0));
//                }
//
//                Vec3 vec34 = this.getDeltaMovement();
//                if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6F - this.getY() + y, vec34.z)) {
//                    this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
//                }
//
//                entity.calculateEntityAnimation(this instanceof FlyingAnimal);
//                ci.cancel();
//            }
//        }
//    }

    @ModifyVariable(
            method = "travel",
            at = @At(
                    value = "STORE",
                    ordinal = 0,
                    // Target the STORE of f3 after the block friction call
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"
            ),
            name = "f3"
    )
    private float modifyFrictionFactorF3(float f3) {
        return FrictionCalculator.handleFriction((((LivingEntity) (Object) this)), f3);
    }

    @ModifyArgs(
            method = "handleRelativeFrictionAndCalculateMovement",
            at = @At(
                    value = "INVOKE",
                    // Targets the exact 'new Vec3(vec3.x, 0.2, vec3.z)' instantiation
                    target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"
            )
    )
    private void modifyClimbUpwardSpeedArgs(Args args) {
        LivingEntity self = (LivingEntity) (Object) this;
        double originalY = args.get(1);

        // Double check we are on the 0.2 branch to avoid false positives
        if (originalY == 0.2D) {
            double customClimbSpeed = 0.2D * self.getAttributeValue(GLAttributes.CLIMBING_SPEED_MODIFIER);
            args.set(1, customClimbSpeed);
        }
    }


    // region FLUID_SWIM ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * The following code was adapted from <a href="https://github.com/florensie/ExpandAbility/blob/fd1fb9c74121dde961d2fc5c621ac85b5420380e/common/src/main/java/be/florens/expandability/mixin/swimming/LivingEntityMixin.java">ExpandAbility</a>, thank you!
     */
    @ModifyExpressionValue(method = "aiStep", require = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D"))
    private double setFluidHeight(double original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(player), 1D, 0D, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = {"travel", "aiStep", "checkFallDamage"},
            require = 3,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private boolean setInWater(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }

    /**
     * GooLib: add support for lava
     */
    @ModifyExpressionValue(method = {"travel", "aiStep"},
            require = 2,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z"))
    private boolean setInLava(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }


    /**
     * Reset the fall distance every tick when swimming is enabled
     */
    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void resetFallHeight(CallbackInfo info) {
        //noinspection ConstantConditions
        if ((Object) this instanceof Player player && PlayerSwimEvent.postAndGetResult(player) == EventResult.SUCCESS) {
            this.fallDistance = 0;
        }
    }

    /**
     * Cancel the small boost upward when leaving a fluid while against the side of a block when swimming is enabled
     */
    @ModifyExpressionValue(method = "travel", allow = 2, require = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isFree(DDD)Z"))
    private boolean cancelLeaveFluidAssist(boolean original) {
        if ((Object) this instanceof Player player) {
            if (PlayerSwimEvent.postAndGetResult(player) == EventResult.SUCCESS) {
                return false;
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "travel", require = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInFluidType(Lnet/minecraft/world/level/material/FluidState;)Z"))
    private boolean setInFluidType(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.shouldPlayerSwim(player, original);
        }

        return original;
    }

    @ModifyExpressionValue(method = "aiStep", require = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/FluidType;isAir()Z"))
    private boolean setIsAir(boolean original) {
        if ((Object) this instanceof Player player) {
            return PlayerSwimEvent.processEventResult(PlayerSwimEvent.postAndGetResult(player), false, true, original);
        }

        return original;
    }
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

}