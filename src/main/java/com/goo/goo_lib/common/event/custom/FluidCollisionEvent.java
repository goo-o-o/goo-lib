package com.goo.goo_lib.common.event.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Fired whenever a LivingEntity makes top-face contact with a fluid
 * This event is {@link ICancellableEvent}
 * If canceled, the LivingEntity will not be able to fall into the fluid
 * Adapted from <a href="https://github.com/florensie/ExpandAbility/blob/master/neoforge/src/main/java/be/florens/expandability/api/forge/LivingFluidCollisionEvent.java">ExpandAbility</a>, thank you!
 */
public class FluidCollisionEvent extends LivingEvent implements ICancellableEvent {
    private final BlockPos pos;
    private final FluidState fluidState;
    private VoxelShape resultShape;

    /**
     * @param entity The LivingEntity making contact with the fluid
     * @param pos The position of the fluid
     * @param fluidState The fluid
     * @param resultShape The shape of the fluid, modifiable
     */
    public FluidCollisionEvent(LivingEntity entity, BlockPos pos, FluidState fluidState, VoxelShape resultShape) {
        super(entity);
        this.pos = pos;
        this.fluidState = fluidState;
        this.resultShape = resultShape;
    }

    public BlockPos getPos() { return this.pos; }
    public VoxelShape getResultShape() { return this.resultShape; }
    public void setResultShape(VoxelShape resultShape) { this.resultShape = resultShape; }
    public FluidState fluidState() { return fluidState; }
}