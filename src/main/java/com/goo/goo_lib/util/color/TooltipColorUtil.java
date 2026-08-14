package com.goo.goo_lib.util.color;

import net.minecraft.Util;
import net.minecraft.util.FastColor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class TooltipColorUtil {
    /**
     * Helper method to set bloom intensity on an array of colors
     * i.e. multiply intensity by 100 and set that as the alpha channel
     */
    public static int[] applyBloomIntensity(int[] colors, float intensity) {
        int alphaByte = Math.clamp((int) (intensity * 100.0F), 0, 255);
        int[] result = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            result[i] = (alphaByte << 24) | (colors[i] & 0x00FFFFFF);
        }
        return result;
    }

    public static int getGradientAt(float x, float spread, float speed, List<Integer> colors) {
        Integer[] integerArray = colors.toArray(new Integer[0]);
        int[] array = ArrayUtils.toPrimitive(integerArray);
        return getGradientAt(x,spread,speed, array);
    }

    public static int getGradientAt(float x, float spread, float speed, int... colors) {
        // Use the game's actual clock for smooth animation that respects pause menus
        // Divide by 1000 to convert millis to seconds
        float time = (Util.getMillis() % 100000L) / 1000.0F;

        // Offset is (position / spread) + (time * speed)
        // We multiply speed by time so the wave moves
        float progress = ((x / spread) + (time * speed)) % 1.0F;
        if (progress < 0) progress += 1.0F;

        int numColors = colors.length;
        float scaledProgress = progress * numColors;
        int index = (int) scaledProgress;
        int nextIndex = (index + 1) % numColors;
        float segmentProgress = scaledProgress - index;

        return FastColor.ARGB32.lerp(segmentProgress, colors[index], colors[nextIndex]);
    }
}
