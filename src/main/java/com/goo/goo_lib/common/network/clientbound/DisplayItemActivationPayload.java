package com.goo.goo_lib.common.network.clientbound;

import com.goo.goo_lib.common.GooLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DisplayItemActivationPayload(
        ItemStack stack
) implements CustomPacketPayload {
    public static final Type<DisplayItemActivationPayload> TYPE = new Type<>(GooLib.loc("display_item_activation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayItemActivationPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            DisplayItemActivationPayload::stack,
            DisplayItemActivationPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}