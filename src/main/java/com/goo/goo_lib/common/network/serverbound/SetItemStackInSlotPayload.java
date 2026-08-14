package com.goo.goo_lib.common.network.serverbound;


import com.goo.goo_lib.common.GooLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record SetItemStackInSlotPayload(int slot, ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetItemStackInSlotPayload> TYPE =
            new Type<>(GooLib.loc("set_item_stack_in_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetItemStackInSlotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SetItemStackInSlotPayload::slot,
            ItemStack.OPTIONAL_STREAM_CODEC,
            SetItemStackInSlotPayload::stack,
            SetItemStackInSlotPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
