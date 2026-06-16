package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.DynamicAttributeAPI;
import com.goo.goo_lib.common.attribute.IDynamicAttribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixin implements IDynamicAttribute {
    @Unique
    private LivingEntity gl$owner;

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void enchanted$directInstanceAccess(CallbackInfoReturnable<Double> cir) {
        if (this.gl$owner != null) {
            double original = cir.getReturnValue();

            // Pass calculation to the clean API wrapper
            double modified = DynamicAttributeAPI.entryPoint(
                    this.gl$owner,
                    (AttributeInstance) (Object) this,
                    original
            );

            cir.setReturnValue(modified);
            
        }
    }

    @Override
    public void gl$setOwner(LivingEntity entity) {
        this.gl$owner = entity;
    }
}
