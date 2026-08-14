package com.goo.goo_lib.util;

import lombok.Getter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MotionBlurUtil {

    public static void setEnabled(boolean enabled) {
        MotionBlurUtil.enabled = enabled;
    }

    @Getter
    private static boolean enabled;

}
