package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.attribute.IDynamicAttribute;
import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    protected abstract boolean isAffectedByFluids();

    @Inject(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void modifyLavaTravel(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.isInLava() && this.isAffectedByFluids()) {

            float efficiency = (float) entity.getAttributeValue(GLAttributes.LAVA_MOVEMENT_EFFICIENCY);

            if (efficiency > 0.0F) {
                double gravity = this.getGravity();
                boolean movingDownwards = this.getDeltaMovement().y <= 0.0;
                if (movingDownwards && entity.hasEffect(MobEffects.SLOW_FALLING)) {
                    gravity = Math.min(gravity, 0.01);
                }

                double y = this.getY();

                BlockPos blockBelow = entity.getBlockPosBelowThatAffectsMyMovement();
                float landFriction = entity.level().getBlockState(blockBelow).getFriction(entity.level(), blockBelow, entity);


                float lavaAcceleration = 0.02F;
                lavaAcceleration += ((float) entity.getAttributeValue(Attributes.MOVEMENT_SPEED) - lavaAcceleration) * efficiency;

                double horizontalDrag = 0.5;
                horizontalDrag += (landFriction - horizontalDrag) * efficiency;

                this.moveRelative(lavaAcceleration, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());

                if (this.getFluidTypeHeight(NeoForgeMod.LAVA_TYPE.value()) <= this.getFluidJumpThreshold()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(horizontalDrag, 0.8F, horizontalDrag));
                    Vec3 vec33 = entity.getFluidFallingAdjustedMovement(gravity, movingDownwards, this.getDeltaMovement());
                    this.setDeltaMovement(vec33);
                } else {
                    Vec3 currentMovement = this.getDeltaMovement();
                    this.setDeltaMovement(currentMovement.x * horizontalDrag, currentMovement.y * 0.5D, currentMovement.z * horizontalDrag);
                }

                if (gravity != 0.0) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -gravity / 4.0, 0.0));
                }

                Vec3 vec34 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6F - this.getY() + y, vec34.z)) {
                    this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
                }

                entity.calculateEntityAnimation(this instanceof FlyingAnimal);
                ci.cancel();
            }
        }
    }
}