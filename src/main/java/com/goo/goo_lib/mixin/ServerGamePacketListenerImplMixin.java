package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.event.custom.EventResult;
import com.goo.goo_lib.common.event.custom.PlayerSwimEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    private boolean clientIsFloating;

    @Shadow
    public ServerPlayer player;

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * The following code is taken from <a href="https://github.com/florensie/ExpandAbility/blob/master/common/src/main/java/be/florens/expandability/mixin/swimming/server/ServerGamePacketListenerImplMixin.java">ExpandAbility</a>, thank you!
     * Avoids getting kicked for flying while swimming is enabled
     */
    @Inject(
            method = "handleMovePlayer",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;clientIsFloating:Z",
                    shift = At.Shift.AFTER,
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void allowSwimFlying(CallbackInfo info) {
        if (PlayerSwimEvent.postAndGetResult(this.player) == EventResult.SUCCESS) {
            this.clientIsFloating = false;
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

}