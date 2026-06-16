package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.attribute.IDynamicAttribute;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin implements IDynamicAttribute {

    @Unique
    private LivingEntity gl$owner;

    @Override
    public void gl$setOwner(LivingEntity entity) {
        this.gl$owner = entity;
    }

    @Inject(method = "getInstance*", at = @At("RETURN"))
    private void gl$tagNewInstance(Holder<Attribute> attribute, CallbackInfoReturnable<AttributeInstance> cir) {
        AttributeInstance instance = cir.getReturnValue();
        if (instance != null && this.gl$owner != null) {
            ((IDynamicAttribute)instance).gl$setOwner(this.gl$owner);
        }
    }

}