package com.goo.goo_lib.util;

// https://easings.net/
// https://gist.github.com/dev-hydrogen/21a66f83f0386123e0c0acf107254843

import lombok.Getter;
import net.minecraft.util.Mth;

import java.util.function.Function;

@Getter
public enum Easing {
    EASE_LINEAR(x -> x),

    // Sine
    EASE_IN_SINE(x -> 1.0 - Math.cos((x * Math.PI) / 2.0)),
    EASE_OUT_SINE(x -> Math.sin((x * Math.PI) / 2.0)),
    EASE_IN_OUT_SINE(x -> -(Math.cos(Math.PI * x) - 1.0) / 2.0),

    // Quad
    EASE_IN_QUAD(x -> x * x),
    EASE_OUT_QUAD(x -> 1.0 - (1.0 - x) * (1.0 - x)),
    EASE_IN_OUT_QUAD(x -> x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0),

    // Cubic
    EASE_IN_CUBIC(x -> x * x * x),
    EASE_OUT_CUBIC(x -> 1.0 - Math.pow(1.0 - x, 3.0)),
    EASE_IN_OUT_CUBIC(x -> x < 0.5 ? 4.0 * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 3.0) / 2.0),

    // Quart
    EASE_IN_QUART(x -> x * x * x * x),
    EASE_OUT_QUART(x -> 1.0 - Math.pow(1.0 - x, 4.0)),
    EASE_IN_OUT_QUART(x -> x < 0.5 ? 8.0 * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 4.0) / 2.0),

    // Quint
    EASE_IN_QUINT(x -> x * x * x * x * x),
    EASE_OUT_QUINT(x -> 1.0 - Math.pow(1.0 - x, 5.0)),
    EASE_IN_OUT_QUINT(x -> x < 0.5 ? 16.0 * x * x * x * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 5.0) / 2.0),

    // Expo
    EASE_IN_EXPO(x -> x == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * x - 10.0)),
    EASE_OUT_EXPO(x -> x == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * x)),
    EASE_IN_OUT_EXPO(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : (x < 0.5 ? Math.pow(2.0, 20.0 * x - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0))),

    // Circ
    EASE_IN_CIRC(x -> 1.0 - Math.sqrt(1.0 - Math.pow(x, 2.0))),
    EASE_OUT_CIRC(x -> Math.sqrt(1.0 - Math.pow(x - 1.0, 2.0))),
    EASE_IN_OUT_CIRC(x -> x < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0),

    // Back
    EASE_IN_BACK(x -> 2.70158 * x * x * x - 1.70158 * x * x),
    EASE_OUT_BACK(x -> 1.0 + 2.70158 * Math.pow(x - 1.0, 3.0) + 1.70158 * Math.pow(x - 1.0, 2.0)),
    EASE_IN_OUT_BACK(x -> {
        double c1 = 1.70158;
        double c2 = c1 * 1.525;
        return x < 0.5
                ? (Math.pow(2.0 * x, 2.0) * ((c2 + 1.0) * 2.0 * x - c2)) / 2.0
                : (Math.pow(2.0 * x - 2.0, 2.0) * ((c2 + 1.0) * (x * 2.0 - 2.0) + c2) + 2.0) / 2.0;
    }),

    // Elastic
    EASE_IN_ELASTIC(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : -Math.pow(2.0, 10.0 * x - 10.0) * Math.sin((x * 10.0 - 10.75) * (2.0 * Math.PI / 3.0)))),
    EASE_OUT_ELASTIC(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * (2.0 * Math.PI / 3.0)) + 1.0)),
    EASE_IN_OUT_ELASTIC(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : (x < 0.5
                                                                 ? -(Math.pow(2.0, 20.0 * x - 10.0) * Math.sin((20.0 * x - 11.125) * (2.0 * Math.PI / 4.5))) / 2.0
                                                                 : (Math.pow(2.0, -20.0 * x + 10.0) * Math.sin((20.0 * x - 11.125) * (2.0 * Math.PI / 4.5))) / 2.0 + 1.0)));

    private final Function<Double, Number> function;

    Easing(Function<Double, Number> function) {
        this.function = function;
    }

    public float ease(float value) {
        return this.ease(value, 0.0F, 1.0F);
    }

    public float ease(float value, float lower, float upper) {
        if (lower >= upper) {
            return value;
        }
        float normalized = (value - lower) / (upper - lower);
        float clamped = Mth.clamp(normalized, 0.0F, 1.0F);
        float easedValue = this.function.apply((double) clamped).floatValue();
        return lower + (upper - lower) * easedValue;
    }

}