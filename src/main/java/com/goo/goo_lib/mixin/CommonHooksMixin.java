package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {
    @Redirect(method = "isLivingOnLadder", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isLadder(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean modifyIsLadder(BlockState state, LevelReader levelReader, BlockPos pos, LivingEntity entity) {
        return state.isLadder(levelReader, pos, entity) || goo_lib$canWallClimb(pos, entity);
    }

    @Unique
    private static boolean goo_lib$canWallClimb(BlockPos pos, LivingEntity entity) {
        if (entity.getAttributeValue(GLAttributes.WALL_CLIMBING) <= 0) return false;
        boolean nearSolidWall = false;

        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

        // Check North, South, East, West
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            neighborPos.set(pos).move(direction);
            BlockState neighborState = entity.level().getBlockState(neighborPos);

            // If any adjacent horizontal block is a solid wall, this position becomes climbable!
            if (!neighborState.isAir() && neighborState.isSolid()) {
                nearSolidWall = true;
            }
        }

        return nearSolidWall;
    }
}
