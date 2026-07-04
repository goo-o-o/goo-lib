package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin {

    @Shadow
    private float invFriction;

    @ModifyVariable(
            method = "controlBoat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            ),
            ordinal = 0
    )
    private float modifyBoatAcceleration(float f) {
        Boat boat = (Boat) (Object) this;
        LivingEntity entity = boat.getControllingPassenger();
        if (entity != null) {
            double modifier = entity.getAttributeValue(GLAttributes.BOAT_SPEED_MODIFIER);

            // Squaring the modifier balances the looser friction curve in floatBoat,
            // making the boat reach its destination speed instantly and crisply.
            double balancedAcceleration = f * (modifier * modifier);

            return (float) balancedAcceleration;
        }
        return f;
    }

    @Inject(
            method = "floatBoat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/Boat;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 1
            )
    )
    private void modifyMaxBoatSpeedLinearly(CallbackInfo ci) {
        Boat boat = (Boat) (Object) this;
        if (boat.getControllingPassenger() instanceof LivingEntity player) {
            double modifier = player.getAttributeValue(GLAttributes.BOAT_SPEED_MODIFIER);

            // 1.0 means normal speed, no change needed
            if (modifier != 1.0) {
                float fVanilla = this.invFriction; // Grab whatever friction status vanilla currently set (water, land, air)

                // Apply the linear terminal velocity scaling formula
                double numerator = modifier * fVanilla;
                double denominator = numerator + (1.0 - fVanilla);

                // Set the new friction, clamping it safely under 0.99F to prevent game-breaking speeds at near-zero denominators
                this.invFriction = (float) Math.min(0.99D, numerator / denominator);
            }
        }
    }
}