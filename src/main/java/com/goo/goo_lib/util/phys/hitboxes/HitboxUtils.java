package com.goo.goo_lib.util.phys.hitboxes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;

import java.util.List;

public class HitboxUtils {

    public static Vec3 getShoulderPosition(LivingEntity livingEntity) {
        double shoulderHeight = (double) livingEntity.getBbHeight() * 0.15 * (double) livingEntity.getScale();
        return livingEntity.getEyePosition().add(0F, -shoulderHeight, 0F);
    }

    public static class RayResult<T> {
        public List<T> entitiesHit;
        public Vec3 endPos;
        public float distance;

        public RayResult(List<T> entitiesHit, Vec3 endPos, float distance) {
            this.entitiesHit = entitiesHit;
            this.endPos = endPos;
            this.distance = distance;
        }
    }
//    public static <T extends Entity> RayResult<T> handleRay(Player player, Class<T> clazz, OrientedBoundingBox hitbox, Vec3 offset, EntityType<? extends BrutalityRay> rayType, boolean needsLos) {
//        Vec3 origin = player.getEyePosition();
//        float zOffset = (float) offset.z;
//        Vec3 look = player.getLookAngle();
//        float maxRange = (float) (hitbox.halfExtents.z() * 2) + zOffset + player.getBbWidth() * 0.5F;
//
//        BlockHitResult blockHit = player.level().clip(new ClipContext(
//                origin,
//                origin.add(look.scale(maxRange + 1.0f)), // +1 buffer to ensure hit
//                ClipContext.Block.COLLIDER,
//                ClipContext.Fluid.NONE,
//                player
//        ));
//
//        float rayLength = maxRange;
//        Vec3 endPos = origin.add(look.scale(maxRange));
//
//        if (blockHit.getType() == HitResult.Type.BLOCK) {
//            rayLength = (float) ((float) blockHit.getLocation().distanceTo(origin) - zOffset + hitbox.halfExtents.z());
//            endPos = blockHit.getLocation();
//        }
//
//        OrientedBoundingBox inWorld = (OrientedBoundingBox) hitbox.inWorld(player, origin, offset);
//        List<T> entitiesHit = inWorld.findEntitiesHit(player, clazz);
//
//        if (rayType != null && player.level() instanceof ServerLevel serverLevel && rayLength > 0) {
//            BrutalityRay ray = rayType.create(serverLevel);
//
//            if (ray != null) {
//                ray.setOwner(player);
//                // Position at start of ray (shoulder + forward offset)
//                ray.setPos(origin.add(look.scale(offset.z - hitbox.halfExtents.z)));
//                ray.setYRot(player.getYRot());
//                ray.setXRot(player.getXRot());
//                // Set length to stop at wall or full range
//                ray.setDataMaxLength(rayLength);
//                serverLevel.addFreshEntity(ray);
//            }
//        }
//        return new RayResult<>(entitiesHit, endPos, rayLength);
//    }
//
    public static Matrix3f fromAngle(float pitch, float yaw, float roll) {
        // MC yaw CCW → negate
        return new Matrix3f()
                .rotateY((float) Math.toRadians(-yaw))   // MC yaw CCW → negate
                .rotateX((float) Math.toRadians(pitch))
                .rotateZ((float) Math.toRadians(roll));
    }
}

