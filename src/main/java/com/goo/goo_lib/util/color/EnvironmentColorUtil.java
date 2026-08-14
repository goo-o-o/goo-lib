package com.goo.goo_lib.util.color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class EnvironmentColorUtil {


    public record ColorOverride(String identifier, int priority, int color) implements Comparable<ColorOverride> {
        @Override
        public int compareTo(ColorOverride o) {
            // Higher priority value takes precedence
            return Integer.compare(o.priority, this.priority);
        }
    }

    // Map each ColorType to a sorted list of overrides
    private static final EnumMap<EnvironmentColorType, List<ColorOverride>> OVERRIDES = new EnumMap<>(EnvironmentColorType.class);

    static {
        for (EnvironmentColorType type : EnvironmentColorType.values()) {
            OVERRIDES.put(type, new ArrayList<>());
        }
    }

    /**
     * Gets the active highest-priority color override for the given type.
     */
    public static Integer getOverride(EnvironmentColorType environmentColorType) {
        List<ColorOverride> list = OVERRIDES.get(environmentColorType);
        return list.isEmpty() ? null : list.getFirst().color();
    }

    /**
     * Sets or updates an override with a specific source and priority.
     */
    public static void setColorOverride(String identifier, EnvironmentColorType type, int priority, int color) {
        List<ColorOverride> list = OVERRIDES.get(type);
        Integer previousActive = getOverride(type);

        // Remove existing override from the same source if present
        list.removeIf(override -> override.identifier().equals(identifier));

        // Add new entry and re-sort by priority descending
        list.add(new ColorOverride(identifier, priority, color));
        Collections.sort(list);

        checkAndApplyStateChange(type, previousActive);
    }

    /**
     * Removes an override from a specific source.
     */
    public static void resetColor(String identifier, EnvironmentColorType type) {
        List<ColorOverride> list = OVERRIDES.get(type);
        Integer previousActive = getOverride(type);

        if (list.removeIf(override -> override.identifier().equals(identifier))) {
            checkAndApplyStateChange(type, previousActive);
        }
    }

    public static void resetAllColorsIdentifier(String identifier) {
        for (Map.Entry<EnvironmentColorType, List<ColorOverride>> entry : OVERRIDES.entrySet()) {
            EnvironmentColorType type = entry.getKey();
            List<ColorOverride> list = entry.getValue();

            Integer previousActive = getOverride(type);

            // Remove matching overrides for this specific color type
            if (list.removeIf(override -> override.identifier().equals(identifier))) {
                checkAndApplyStateChange(type, previousActive);
            }
        }
    }

    /**
     * Removes all overrides across all sources.
     */
    public static void resetAllColors() {
        boolean needsReload = false;

        for (Map.Entry<EnvironmentColorType, List<ColorOverride>> entry : OVERRIDES.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                entry.getValue().clear();
                if (entry.getKey().requiresReload) {
                    needsReload = true;
                }
            }
        }

        if (needsReload) {
            Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    private static void checkAndApplyStateChange(EnvironmentColorType type, Integer previousActive) {
        Integer currentActive = getOverride(type);

        // Only reload chunks or update fog if the top active color actually changed
        if (!Objects.equals(previousActive, currentActive)) {
            if (type.requiresReload) {
                Minecraft.getInstance().levelRenderer.allChanged();
            }
            if (type == EnvironmentColorType.WATER_FOG) {
                FogRenderer.biomeChangedTime = -1L;
            }
        }
    }
}