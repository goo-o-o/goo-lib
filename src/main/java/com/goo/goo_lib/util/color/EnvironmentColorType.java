package com.goo.goo_lib.util.color;

public enum EnvironmentColorType {
    FOG(false),
    WATER(true),
    WATER_FOG(false),
    SKY(false),
    FOLIAGE(true),
    GRASS(true);

    public final boolean requiresReload;

    EnvironmentColorType(boolean requiresReload) {
        this.requiresReload = requiresReload;
    }
}
