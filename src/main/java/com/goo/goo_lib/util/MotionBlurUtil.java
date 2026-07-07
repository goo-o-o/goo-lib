package com.goo.goo_lib.util;

public class MotionBlurUtil {
    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        MotionBlurUtil.enabled = enabled;
    }

    private static boolean enabled;

}
