package org.jurassicraft.server.entity.ai.metabolism;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jurassicraft.client.model.animation.DinosaurAnimation;
import org.jurassicraft.server.entity.DinosaurEntity;
import org.jurassicraft.server.entity.MetabolismContainer;
import org.jurassicraft.server.entity.ai.util.OnionTraverser;
import org.jurassicraft.server.food.FoodHelper;
import org.jurassicraft.server.util.GameRuleHandler;

public class GrazeEntityAI extends EntityAIBase {
    public static final int EAT_RADIUS = 6;// was 25
    public static final int LOOK_RADIUS = 16;
    private static final int GIVE_UP_TIME = 200;// 7*20 counter = 7 seconds (ish?
    private static final Logger LOGGER = LogManager.getLogger();

    protected DinosaurEntity dinosaur;
    protected BlockPos target;
    private int counter;
    private World world;
    private BlockPos previousTarget;
    private Vec3d targetVec;

    public GrazeEntityAI(DinosaurEntity dinosaur) {
        this.dinosaur = dinosaur;
    }

    @Override
    public boolean shouldExecute() {
        return !(this.dinosaur.isDead || this.dinosaur.isCarcass() || !GameRuleHandler.DINO_METABOLISM.getBoolean(this.dinosaur.worldObj)) && this.dinosaur.getMetabolism().isHungry() && this.dinosaur.getClosestFeeder() == null;
    }

    @Override
    public void startExecuting() {
        // This gets called once to initiate.  Here's where we find the plant and start movement
        Vec3d headPos = this.dinosaur.getHeadPos();
        BlockPos head = new BlockPos(headPos.xCoord, headPos.yCoord, headPos.zCoord);

        //world the animal currently inhabits
        this.world = this.dinosaur.worldObj;

        MetabolismContainer metabolism = this.dinosaur.getMetabolism();

        // Look in increasing layers (e.g. boxes) around the head. Traversers... are like ogres?
        OnionTraverser traverser = new OnionTraverser(head, LOOK_RADIUS);
        this.target = null;

        //scans all blocks around the LOOK_RADIUS
        for (BlockPos pos : traverser) {
            Block block = this.world.getBlockState(pos).getBlock();

            if (FoodHelper.isEdible(this.dinosaur.getDinosaur().getDiet(), block) && pos != this.previousTarget) {
                this.target = pos;
                this.targetVec = new Vec3d(this.target.getX(), this.target.getY(), this.target.getZ());
                break;
            }
        }

        if (this.target != null && metabolism.isStarving()) {
            this.dinosaur.getNavigator().tryMoveToXYZ(this.target.getX(), this.target.getY(), this.target.getZ(), 1.2);
        } else if (this.target != null) {
            this.dinosaur.getNavigator().tryMoveToXYZ(this.target.getX(), this.target.getY(), this.target.getZ(), 0.7);
        }
    }

    @Override
    public boolean continueExecuting() {
        if (this.target != null && this.world.isAirBlock(this.target) && !this.dinosaur.getNavigator().noPath()) {
            this.terminateTask();
            return false;
        }

        return this.target != null;
    }

    @Override
    public void updateTask() {
        if (this.target != null) {
            Vec3d headPos = this.dinosaur.getHeadPos();
            Vec3d headVec = new Vec3d(headPos.xCoord, this.target.getY(), headPos.zCoord);

            if (headVec.squareDistanceTo(this.targetVec) < EAT_RADIUS) {
                this.dinosaur.getNavigator().clearPathEntity();

                // TODO inadequate method for looking at block
                this.dinosaur.getLookHelper().setLookPosition(this.target.getX(), this.target.getY(), this.target.getZ(), 30.0F, this.dinosaur.getVerticalFaceSpeed());

                this.dinosaur.setAnimation(DinosaurAnimation.EATING.get());

                Item item = Item.getItemFromBlock(this.world.getBlockState(this.target).getBlock());

                this.world.destroyBlock(this.target, false);

                this.dinosaur.getMetabolism().eat(FoodHelper.getHealAmount(item));
                FoodHelper.applyEatEffects(this.dinosaur, item);
                this.dinosaur.heal(10.0F);

                this.previousTarget = null;
                this.terminateTask();
            } else {
                this.counter++;

                if (this.counter >= GIVE_UP_TIME) {
                    this.counter = 0;
                    this.previousTarget = this.target;
                    this.terminateTask();
                }
            }
        }
    }

    private void terminateTask() {
        this.dinosaur.getNavigator().clearPathEntity();
        this.target = null;
        this.dinosaur.setAnimation(DinosaurAnimation.IDLE.get());
    }
}
