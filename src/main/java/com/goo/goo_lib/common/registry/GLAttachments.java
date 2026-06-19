package com.goo.goo_lib.common.registry;

import com.goo.goo_lib.common.GooLib;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class GLAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, GooLib.MOD_ID);


    public static final Supplier<AttachmentType<Float>> ARROW_GRAVITY =
            ATTACHMENT_TYPES.register("arrow_gravity", () ->
                    AttachmentType.builder(() -> 1.0F)
                            .serialize(Codec.FLOAT)
                            .sync((holder, targetPlayer) -> holder == targetPlayer, ByteBufCodecs.FLOAT)
                            .build()
            );

}
