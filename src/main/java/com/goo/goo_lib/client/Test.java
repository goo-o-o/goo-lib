package com.goo.goo_lib.client;

//@EventBusSubscriber(modid = GooLib.MOD_ID, value = Dist.CLIENT)
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
//    @SubscribeEvent
//    public static void onClientChat(ClientChatEvent event) {
//        if (event.getMessage().equals("Spawned")) {
//            GuiParticle particle = new GuiParticle(ParticleTypes.BUBBLE, 200, 200, 400, 0.5F, 0.0F, 100, 1F, 1F, 1F, 1F, 1F) {
//                @Override
//                public boolean tick() {
//                    this.scale += 0.1F;
//                    return super.tick();
//                }
//            };
//
//            GuiParticleSystem.getInstance().add(particle);
//        }
//    }
}
