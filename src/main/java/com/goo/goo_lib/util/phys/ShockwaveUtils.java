package com.goo.goo_lib.util.phys;

import com.goo.goo_lib.client.particle.WaveParticleOption;
import com.goo.goo_lib.util.DelayedTaskScheduler;
import com.goo.goo_lib.util.Easing;
import com.goo.goo_lib.util.phys.hitboxes.CylindricalBoundingBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ShockwaveUtils {


    public static <T extends Entity> List<T> applyWaveEffect(ServerLevel level, Vec3 origin, Class<T> clazz, WaveParticleOption particleData,@Nullable Predicate<T> filter, Consumer<Entity> effect) {
        return applyWaveEffect(level, origin.x(), origin.y(), origin.z(), clazz, particleData, filter, effect);
    }

    public static <T extends Entity> List<T> applyWaveEffect(ServerLevel level, Entity origin, Class<T> clazz, WaveParticleOption particleData, @Nullable Predicate<T> filter, Consumer<Entity> effect) {
        return applyWaveEffect(level, origin.getX(), origin.getY(0.5), origin.getZ(), clazz, particleData, filter, effect);
    }

    public static <T extends Entity> List<T> applyWaveEffect(ServerLevel level, double x, double y, double z, Class<T> clazz, WaveParticleOption particleData, @Nullable Predicate<T> filter, Consumer<Entity> effect) {
        float maxRadius = particleData.radius();
        Set<T> affectedEntities = new HashSet<>();
        int ticksPerCheck = 1;
        Vec3 center = new Vec3(x, y, z);

        float growthDuration = particleData.growthDuration();
        Easing easing = particleData.easing();
        for (int age = 0; age <= growthDuration; age += ticksPerCheck) {

            float growthProgress = ((float) age) / growthDuration;
            growthProgress = Mth.clamp(growthProgress, 0.0F, 1.0F);
            final float currentRadius = maxRadius * easing.ease(growthProgress);
            float previousGrowthProgress = (float) (age - 1) / growthDuration;
            previousGrowthProgress = Mth.clamp(previousGrowthProgress, 0.0F, 1.0F);
            final float previousRadius = maxRadius * easing.ease(previousGrowthProgress);

            DelayedTaskScheduler.queueCommonWork(level, age, () -> {
                CylindricalBoundingBox circle = new CylindricalBoundingBox(center, 0.1F, currentRadius, previousRadius);

                List<T> entities = level.getEntitiesOfClass(clazz, circle.getAABB().inflate(2), entity ->
                        (filter == null || filter.test(entity)) && circle.intersectsAABB(entity.getBoundingBox())
                );
                for (T entity : entities) {
                    if (affectedEntities.add(entity) && entity.isAlive()) {
                        effect.accept(entity);
                    }
                }
            });

        }

        return new ArrayList<>(affectedEntities);
    }

}
