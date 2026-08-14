package com.goo.goo_lib.util.screenshake;

import com.goo.goo_lib.util.MotionBlurUtil;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ScreenShakeUtil {
    // per player on client
    private static final Map<String, ShakeInstance> ACTIVE_SHAKES = new HashMap<>();

    /**
     * Safely register a completely custom configured screen shake layer.
     */
    public static void addShake(ShakeInstance instance) {
        if (instance.motionBlur) {
            MotionBlurUtil.setEnabled(true);
        }

        // if exists, we merge them taking the max values
        ACTIVE_SHAKES.compute(instance.identifier, (id, existing) -> {
            if (existing == null) return instance;

            // capture where the shake currently is relative to its old life cycle
            int originalRemainingTicks = existing.durationTicks - existing.getTicksElapsed();

            // get the new duration, whatever is higher
            int baselineNewDuration = Math.max(existing.durationTicks, instance.durationTicks);

            // if the new instance implies a longer remaining time than what we have left, we shift the timeline
            if (instance.durationTicks > originalRemainingTicks) {
                // update the lifetime of the original to the new one
                existing.durationTicks = baselineNewDuration;

                // set ticks elapsed backwards by whatever the added time was
                existing.setTicksElapsed(existing.durationTicks - instance.durationTicks);

                // clamp
                if (existing.getTicksElapsed() < 0) {
                    existing.setTicksElapsed(0);
                }
            }

            // 3. Update intensity configurations cleanly
            existing.speed = Math.max(existing.speed, instance.speed);
            existing.maxX = Math.max(existing.maxX, instance.maxX);
            existing.maxY = Math.max(existing.maxY, instance.maxY);
            existing.maxPitch = Math.max(existing.maxPitch, instance.maxPitch);
            existing.maxYaw = Math.max(existing.maxYaw, instance.maxYaw);
            existing.maxRoll = Math.max(existing.maxRoll, instance.maxRoll);
            existing.motionBlur = existing.motionBlur || instance.motionBlur;

            return existing;
        });
    }

    public static void clearShakes() {
        ACTIVE_SHAKES.clear();
        MotionBlurUtil.setEnabled(false);
    }

    /**
     * Different {@link ShakeInstance}s can have the same identifier
     */
    public static void removeShake(String identifier) {
        ACTIVE_SHAKES.remove(identifier);
    }

    public static void clientTick() {
        // updates instances and sweeps out expired objects automatically
        ACTIVE_SHAKES.values().removeIf(ShakeInstance::tick);
        if (ACTIVE_SHAKES.isEmpty()) {
            MotionBlurUtil.setEnabled(false);
        }
    }

    public static boolean shouldScreenShake() {
        return !Minecraft.getInstance().isPaused();
    }

    /**
     * Merges all running shake calculations together for processing.
     */
    public static ShakeInstance.CalculatedOffsets getCompositeOffsets(float partialTicks) {
        if (ACTIVE_SHAKES.isEmpty()) return ShakeInstance.CalculatedOffsets.ZERO;

        float totalX = 0, totalY = 0, totalPitch = 0, totalYaw = 0, totalRoll = 0;

        for (ShakeInstance shake : ACTIVE_SHAKES.values()) {
            ShakeInstance.CalculatedOffsets offsets = shake.getOffsets(partialTicks);
            totalX += offsets.x();
            totalY += offsets.y();
            totalPitch += offsets.pitch();
            totalYaw += offsets.yaw();
            totalRoll += offsets.roll();
        }

        return new ShakeInstance.CalculatedOffsets(totalX, totalY, totalPitch, totalYaw, totalRoll);
    }

    /**
     * Returns the maximum {@link com.goo.goo_lib.util.screenshake.ShakeInstance.CalculatedOffsets} from active shakes
     */
    public static ShakeInstance.CalculatedOffsets getMaximumOffsets(float partialTicks) {
        if (ACTIVE_SHAKES.isEmpty()) return ShakeInstance.CalculatedOffsets.ZERO;

        float maxX = 0, maxY = 0, maxPitch = 0, maxYaw = 0, maxRoll = 0;

        for (ShakeInstance shake : ACTIVE_SHAKES.values()) {
            ShakeInstance.CalculatedOffsets offsets = shake.getOffsets(partialTicks);
            if (offsets.x() > maxX) maxX = offsets.x();
            if (offsets.y() > maxY) maxY = offsets.y();
            if (offsets.pitch() > maxPitch) maxPitch = offsets.pitch();
            if (offsets.yaw() > maxYaw) maxYaw = offsets.yaw();
            if (offsets.roll() > maxRoll) maxRoll = offsets.roll();
        }

        return new ShakeInstance.CalculatedOffsets(maxX, maxY, maxPitch, maxYaw, maxRoll);
    }
}