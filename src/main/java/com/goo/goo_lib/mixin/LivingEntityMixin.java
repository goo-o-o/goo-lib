package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.attribute.IDynamicAttribute;
import com.goo.goo_lib.common.event.custom.EventResult;
import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import com.goo.goo_lib.common.registry.GLAttributes;
import com.goo.goo_lib.util.phys.PhysicsUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private void assignOwnerToAttributeMap(EntityType<?> type, Level level, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.getAttributes() instanceof IDynamicAttribute duckMap) {
            duckMap.gl$setOwner(self);
        }
    }

    @Shadow
    protected int useItemRemaining;

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);

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

        // only if attribute is less negative
        if (drawSpeed < 1.0 && drawSpeed > 0.0) {
            int mod = (int) Math.round(1.0 / drawSpeed); // 0.25 becomes 4

            // if its not the every-nth tick where we should increase tickCount
            if (instance.tickCount % mod != 0) {
                // just do nothing, do not decrement useItemRemaining
                return;
            }
        }

        // else modify useItemRemaining
        this.useItemRemaining = assignedValue;
    }



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
        return PhysicsUtils.handleFriction((((LivingEntity) (Object) this)), f3);
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

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 1))
    private Vec3 modifyElytraAcceleration(Vec3 instance, double x, double y, double z) {
        double multiplier = getAttributeValue(GLAttributes.ELYTRA_ACCELERATION_MODIFIER);
        return instance.add(x * multiplier, y * multiplier, z * multiplier);
    }

    //region FLUID_SWIM ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

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
    //endregion ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

}