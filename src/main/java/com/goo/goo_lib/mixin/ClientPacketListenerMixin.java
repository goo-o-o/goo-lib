package com.goo.goo_lib.mixin;


import com.goo.goo_lib.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {


    @Shadow
    private ClientLevel level;


    /**
     * Thank you Team CoFH for the code, the reason I used this code can be found in {@link ClientboundSetEntityMotionPacketMixin}
     */
    @Inject(
            method = "handleSetEntityMotion",
            at = @At("HEAD"),
            cancellable = true
    )
    public void handle(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (!ModList.get().isLoaded("cofh_core")) {
            PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, Minecraft.getInstance());
            Entity entity = level.getEntity(packet.getId());

            if (entity != null) {
                ClientboundSetEntityMotionPacketAccessor accessor = (ClientboundSetEntityMotionPacketAccessor) packet;

                float xVal = MathUtils.toFloat(accessor.getRawXa());
                float yVal = MathUtils.toFloat(accessor.getRawYa());
                float zVal = MathUtils.toFloat(accessor.getRawZa());

                entity.lerpMotion(xVal, yVal, zVal);
            }
            ci.cancel();
        }
    }

}