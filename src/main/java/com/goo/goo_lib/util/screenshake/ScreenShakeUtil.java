package com.goo.goo_lib.util.screenshake;

import com.goo.goo_lib.util.MotionBlurUtil;

import java.util.ArrayList;
import java.util.List;

public class ScreenShakeUtil {
    private static final List<ShakeInstance> ACTIVE_SHAKES = new ArrayList<>();

    /**
     * Safely register a completely custom configured screen shake layer.
     */
    public static void addShake(ShakeInstance instance) {
        if (instance.motionBlur)
            MotionBlurUtil.setEnabled(true);
        ACTIVE_SHAKES.add(instance);
    }

    public static void clientTick() {
        // updates instances and sweeps out expired objects automatically
        ACTIVE_SHAKES.removeIf(ShakeInstance::tick);
        if (ACTIVE_SHAKES.isEmpty()) {
            MotionBlurUtil.setEnabled(false);
        }
    }

    /**
     * Merges all running shake calculations together for processing.
     */
    public static ShakeInstance.CalculatedOffsets getCompositeOffsets(float partialTicks) {
        if (ACTIVE_SHAKES.isEmpty()) return ShakeInstance.CalculatedOffsets.ZERO;

        float totalX = 0, totalY = 0, totalPitch = 0, totalYaw = 0, totalRoll = 0;

        for (ShakeInstance shake : ACTIVE_SHAKES) {
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

        for (ShakeInstance shake : ACTIVE_SHAKES) {
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