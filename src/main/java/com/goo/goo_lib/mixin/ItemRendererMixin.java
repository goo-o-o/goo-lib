package com.goo.goo_lib.mixin;

import com.goo.goo_lib.client.registry.GLRenderTypes;
import com.goo.goo_lib.client.render.model.InflatedBakedModel;
import com.goo.goo_lib.util.ItemOutlineUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract void renderModelLists(BakedModel model, ItemStack stack, int combinedLight, int combinedOverlay, PoseStack poseStack, VertexConsumer buffer);

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getRenderPasses(Lnet/minecraft/world/item/ItemStack;Z)Ljava/util/List;")
    )
    private void renderAndBakeOutlineFirst(
            ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand,
            PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight,
            int combinedOverlay, BakedModel p_model, CallbackInfo ci
    ) {
        if (p_model instanceof InflatedBakedModel inflatedModel && !itemStack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            float partialTick = mc.getTimer().getGameTimeDeltaTicks();

            float[] colors = ItemOutlineUtil.getColorComponents(itemStack, partialTick);
            RenderType outlineRenderType = GLRenderTypes.getItemOutlineRenderType(colors);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(outlineRenderType);
            this.renderModelLists(inflatedModel, itemStack, combinedLight, combinedOverlay, poseStack, vertexConsumer);

            if (bufferSource instanceof MultiBufferSource.BufferSource batchedSource) {
                batchedSource.endBatch(outlineRenderType);
            }
        }
    }

}
