package com.goo.goo_lib.client;

import com.goo.goo_lib.common.GooLib;
import com.goo.goo_lib.util.Easing;
import com.goo.goo_lib.util.screenshake.ScreenShakeUtil;
import com.goo.goo_lib.util.screenshake.ShakeInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;

@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
public class Test {
    //    @SubscribeEvent
//    public static void onTooltip(ItemTooltipEvent event) {
//        if (event.getItemStack().is(Items.GOLDEN_SWORD) && !event.getToolTip().isEmpty()) {
//            MutableComponent name = (MutableComponent) event.getToolTip().getFirst();
//            ConfiguredEffect<GradientConfig> wave = new ConfiguredEffect<>(
//                    TextEffects.COLOR_GRADIENT_TYPE.get(), new ColorWaveEffect(),
//                    new GradientConfig(List.of(0xFFD700, 0xFF8C00, 0xFF4500), 150.0f, 4.0f)
//            );
//            ConfiguredEffect<FireConfig> fire = new ConfiguredEffect<>(
//                    TextEffects.FIRE_TYPE.get(),
//                    new FireEffect(),
//                    new FireConfig());
//            ConfiguredEffect<FoggyConfig> abyssal = new ConfiguredEffect<>(
//                    TextEffects.FOGGY_TYPE.get(),
//                    new FoggyEffect(),
//                    new FoggyConfig());
//
//            name.setStyle(StyleEffectUtils.createStyleWithEffects(
//                    Style.EMPTY.withBold(true),
//                    List.of(abyssal)));
//        }
//    }
//
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        if (event.getMessage().equals("Explosion")) {
            ScreenShakeUtil.addShake(new ShakeInstance.Builder()
                    .duration(100) // 1.5 seconds
                    .easeIn(Easing.EASE_IN_EXPO, 15)
                    .easeOut(Easing.EASE_OUT_EXPO, 15)
                    .bounds(100F, 100F)
                    .speed(100)
                    .build());
        } else if (event.getMessage().equals("Rumble")) {
            ScreenShakeUtil.addShake(new ShakeInstance.Builder()
                    .duration(90) // 3 seconds
                    .bounds(3.0F, 3.0F)
                    .speed(2.0F)
                    .build());
        }
    }
}
