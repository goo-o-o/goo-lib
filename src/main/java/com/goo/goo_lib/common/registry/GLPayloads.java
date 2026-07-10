package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.common.network.ScreenShakePayload;
import com.goo.goo_lib.util.screenshake.ScreenShakeUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = GooLib.MOD_ID)
public class GLPayloads {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ScreenShakePayload.TYPE,
                ScreenShakePayload.STREAM_CODEC,
                ScreenShakeUtil::handleScreenShakePacket
        );
    }

}
