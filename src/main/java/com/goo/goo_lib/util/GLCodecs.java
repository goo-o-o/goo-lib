package com.goo.goo_lib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class GLCodecs {

    private static final Codec<Integer> HEX_STRING_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                if (!str.startsWith("#")) return DataResult.error(() -> "Hex color must start with '#'");
                try {
                    String hex = str.substring(1);
                    if (hex.length() == 6) {
                        return DataResult.success(0xFF000000 | Integer.parseInt(hex, 16));
                    } else if (hex.length() == 8) {
                        return DataResult.success((int) Long.parseLong(hex, 16));
                    }
                    return DataResult.error(() -> "Hex color must be 6 or 8 characters long");
                } catch (NumberFormatException e) {
                    return DataResult.error(() -> "Invalid hex color format: " + str);
                }
            },
            color -> String.format("#%08X", color)
    );

    private static final Codec<Integer> VANILLA_VEC3_CODEC = ExtraCodecs.VECTOR3F.xmap(
            vec -> FastColor.ARGB32.colorFromFloat(1.0F, vec.x(), vec.y(), vec.z()),
            color -> new Vector3f(
                    FastColor.ARGB32.red(color) / 255.0F,
                    FastColor.ARGB32.green(color) / 255.0F,
                    FastColor.ARGB32.blue(color) / 255.0F
            )
    );


    private static final Codec<Integer> VANILLA_VEC4_CODEC = ExtraCodecs.VECTOR4F.xmap(
            vec -> FastColor.ARGB32.colorFromFloat(vec.w(), vec.x(), vec.y(), vec.z()),
            color -> new Vector4f(
                    FastColor.ARGB32.red(color) / 255.0F,
                    FastColor.ARGB32.green(color) / 255.0F,
                    FastColor.ARGB32.blue(color) / 255.0F,
                    FastColor.ARGB32.alpha(color) / 255.0F
            )
    );

    public static final Codec<Integer> UNIVERSAL_COLOR_CODEC = NeoForgeExtraCodecs.withAlternative(
            Codec.INT,
            NeoForgeExtraCodecs.withAlternative(
                    HEX_STRING_CODEC,
                    NeoForgeExtraCodecs.withAlternative(VANILLA_VEC4_CODEC, VANILLA_VEC3_CODEC)
            )
    );

    public static final Codec<Vector2f> VECTOR2F = Codec.FLOAT
            .listOf()
            .comapFlatMap(
                    floats -> Util.fixedSize(floats, 2).map(floatList -> new Vector2f(floatList.getFirst(), floatList.get(1))),
                    s -> List.of(s.x(), s.y()));


}
