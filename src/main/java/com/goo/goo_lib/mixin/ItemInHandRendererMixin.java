package com.goo.goo_lib.mixin;


import com.goo.goo_lib.common.registry.GLAttributes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private int smooth_lastTicksUsing = 0;
    @Unique
    private float smooth_ticksElapsed = 0.0F;
    @Unique
    private boolean smooth_shouldOverride = false;
    @Unique
    private float smooth_mockValue = 0.0F;

    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD")
    )
    private void preCalculateSmoothTension(
            AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
            float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight, CallbackInfo ci
    ) {
        smooth_shouldOverride = false;

        if (!(player instanceof LocalPlayer localPlayer) || !(stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)) {
            return;
        }

        AttributeInstance drawTimeAttr = localPlayer.getAttribute(GLAttributes.DRAW_SPEED);
        if (drawTimeAttr == null) {
            return;
        }

        double targetSeconds = drawTimeAttr.getValue();
        if (targetSeconds <= 0.05) targetSeconds = 0.05;

        double vanillaBaseTicks = stack.getItem() instanceof CrossbowItem ? CrossbowItem.getChargeDuration(stack, localPlayer) : 20.0;
        float speedMultiplier = (float) (vanillaBaseTicks / (targetSeconds * 20.0));

        int currentTicksUsing = localPlayer.getTicksUsingItem();
        if (currentTicksUsing <= 0 || currentTicksUsing < smooth_lastTicksUsing) {
            smooth_ticksElapsed = 0.0F;
        }
        smooth_lastTicksUsing = currentTicksUsing;

        float deltaFrameTime = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
        smooth_ticksElapsed += deltaFrameTime * speedMultiplier;

        // Algebraically solve for the exact value that makes vanilla's formula output our smooth progression:
        // Vanilla: useDuration - (remainingTicks - partialTicks + 1.0F) = smooth_ticksElapsed
        smooth_mockValue = (float) stack.getUseDuration(localPlayer) - smooth_ticksElapsed + partialTicks - 1.0F;
        smooth_shouldOverride = true;
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I"
            )
    )
    private int bypassJitteryRemainingTicks(AbstractClientPlayer instance) {
        if (smooth_shouldOverride) {
            return (int) smooth_mockValue;
        }
        return instance.getUseItemRemainingTicks();
    }
}