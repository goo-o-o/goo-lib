package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @ModifyConstant(
        method = "startUseItem",
        constant = @Constant(intValue = 4)
    )
    private int modifyRightClickDelay(int original) {
        if (this.player != null) {
            return (int) this.player.getAttributeValue(GLAttributes.RIGHT_CLICK_DELAY);
        }
        return original;
    }
}