package org.jurassicraft.server.entity.ai.metabolism;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jurassicraft.server.block.BlockHandler;
import org.jurassicraft.server.block.entity.FeederBlockEntity;
import org.jurassicraft.server.entity.DinosaurEntity;
import org.jurassicraft.server.util.GameRuleHandler;

public class FeederEntityAI extends EntityAIBase {
    protected DinosaurEntity dinosaur;

    protected Path path;
    protected BlockPos pos;

    public FeederEntityAI(DinosaurEntity dinosaur) {
        this.dinosaur = dinosaur;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.dinosaur.isDead && !this.dinosaur.isCarcass() && !this.dinosaur.isMovementBlocked() && this.dinosaur.ticksExisted % 16 == 0 && GameRuleHandler.DINO_METABOLISM.getBoolean(this.dinosaur.worldObj)) {
            if (this.dinosaur.getMetabolism().isHungry()) {
                BlockPos feeder = this.dinosaur.getClosestFeeder();

                if (feeder != null) {
                    this.pos = feeder;
                    this.path = this.dinosaur.getNavigator().getPathToPos(this.pos);
                    return this.dinosaur.getNavigator().setPath(this.path, 1.0);
                }
            }
        }

        return false;
    }

    @Override
    public void updateTask() {
        if (!this.dinosaur.worldObj.isRemote && this.dinosaur.getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) <= this.dinosaur.width * 2.0) {
            TileEntity tile = this.dinosaur.worldObj.getTileEntity(this.pos);

            if (tile instanceof FeederBlockEntity) {
                FeederBlockEntity feeder = (FeederBlockEntity) tile;
                feeder.setOpen(true);
                feeder.setFeeding(this.dinosaur);
            }

            this.resetTask();
        }
    }

    @Override
    public void resetTask() {
        this.dinosaur.getNavigator().clearPathEntity();
    }

    @Override
    public boolean continueExecuting() {
        Block block = this.dinosaur.worldObj.getBlockState(this.pos).getBlock();

        return this.dinosaur != null && this.path != null && !this.dinosaur.getNavigator().noPath() && block == BlockHandler.FEEDER;
    }
}
