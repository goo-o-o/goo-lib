package com.goo.goo_lib.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetEntityMotionPacket.class)
public interface ClientboundSetEntityMotionPacketAccessor {
    @Accessor("xa")
    int getRawXa();

    @Accessor("ya")
    int getRawYa();

    @Accessor("za")
    int getRawZa();
}