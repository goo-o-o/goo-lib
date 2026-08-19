package com.goo.goo_lib.util.phys.hitboxes;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Base class for my bounding boxes
 */
@Getter
public abstract class BaseBoundingBox {

    @Setter
    public Vec3 center;

    public Matrix3f rotation = new Matrix3f(); // every hitbox can be rotated

    /**
     * Returns a list of points in world-space used to check for block collisions.
     */
    public abstract AABB getAABB();

    public abstract BaseBoundingBox copy();

    public abstract void render(PoseStack poseStack);

    public BaseBoundingBox translateWorld(Vec3 v) {
        center = center.add(v);
        return this;
    }

    public abstract boolean intersectsAABB(AABB aabb);

    public BaseBoundingBox translateLocal(Vec3 local) {
        Vector3f t = local.toVector3f();
        rotation.transform(t);
        center = center.add(t.x, t.y, t.z);
        return this;
    }

    public BaseBoundingBox rotateWorld(Matrix3f rot) {
        rotation.mul(rot);
        return this;
    }

    public void rotateLocal(Matrix3f rot) {
        rotation.mulLocal(rot);
    }

    public BaseBoundingBox inflate(float f) {
        return this;
    }

    public BaseBoundingBox inWorld(LivingEntity livingEntity, Vec3 localOffset) {
        return this.copy()
                .translateWorld(HitboxUtils.getShoulderPosition(livingEntity))
                .rotateWorld(HitboxUtils.fromAngle(livingEntity.getXRot(), livingEntity.getYRot(), 0))
                .translateLocal(localOffset.add(0,0, livingEntity.getBbWidth() / 2));
    }

    public BaseBoundingBox inWorld(LivingEntity livingEntity, Vec3 localOffset, float xRot, float yRot) {
        return this.copy()
                .translateWorld(HitboxUtils.getShoulderPosition(livingEntity))
                .rotateWorld(HitboxUtils.fromAngle(xRot, yRot, 0))
                .translateLocal(localOffset.add(0,0, livingEntity.getBbWidth() / 2));
    }

    public BaseBoundingBox inWorld(LivingEntity livingEntity, Vec3 origin, Vec3 localOffset) {
        return this.copy()
                .translateWorld(origin)
                .rotateWorld(HitboxUtils.fromAngle(livingEntity.getXRot(), livingEntity.getYRot(), 0))
                .translateLocal(localOffset.add(0,0, livingEntity.getBbWidth() / 2));
    }

    public <T extends Entity> List<T> filter(List<T> entities) {
        return entities.stream().filter(entity -> intersectsAABB(entity.getBoundingBox())).toList();
    }

    public abstract <T extends Entity> List<T> findEntitiesHit(Player player, Class<T> clazz);


    /**
     * Set the rotation matrix
     */
    public void setRotationMatrix(Matrix3f rotation) {
        this.rotation = rotation;
    }
}