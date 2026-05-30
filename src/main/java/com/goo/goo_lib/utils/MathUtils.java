package com.goo.goo_lib.utils;

import org.spongepowered.asm.mixin.Unique;

public class MathUtils {
    @Unique
    public static float toFloat(int hbits) {

        int mant = hbits & 0x03ff;
        int exp = hbits & 0x7c00;
        if (exp == 0x7c00) {
            exp = 0x3fc00;
        } else if (exp != 0) {
            exp += 0x1c000;
            if (mant == 0 && exp > 0x1c400) {
                return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
            }
        } else if (mant != 0) {
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            } while ((mant & 0x400) == 0);
            mant &= 0x3ff;
        }
        return Float.intBitsToFloat((hbits & 0x8000) << 16 | (exp | mant) << 13);
    }

    /**
     * Converts a 32-bit float to a 16-bit half-precision float representation stored in an int.
     * This preserves precision for high-velocity entities (projectiles) where vanilla's
     * standard compression would cause jitter or trajectory snapping.
     * * Credits: Original logic by Hesperos; refined by Team CoFH.
     * * @param fval The 32-bit float velocity component
     *
     * @return A 16-bit encoded representation of the float
     */
    @Unique
    public static int packHalfFloat(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = fbits >>> 16 & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        // Handle Exponent Overflow
        if (val >= 0x47800000) {
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000) {
                    return sign | 0x7c00;
                }
                return sign | 0x7c00 | (fbits & 0x007fffff) >>> 13;
            }
            return sign | 0x7bff;
        }

        // Standard Normalized Range
        if (val >= 0x38800000) {
            return sign | val - 0x38000000 >>> 13;
        }

        // Subnormal Range / Zero
        if (val < 0x33000000) {
            return sign;
        }

        val = (fbits & 0x7fffffff) >>> 23;
        return sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val);
    }
}
