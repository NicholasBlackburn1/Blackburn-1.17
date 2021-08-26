package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class WallClimberNavigation extends GroundPathNavigation
{
    private BlockPos pathToPosition;

    public WallClimberNavigation(Mob p_26580_, Level p_26581_)
    {
        super(p_26580_, p_26581_);
    }

    public Path createPath(BlockPos pPos, int pAccuracy)
    {
        this.pathToPosition = pPos;
        return super.createPath(pPos, pAccuracy);
    }

    public Path createPath(Entity pPos, int pAccuracy)
    {
        this.pathToPosition = pPos.blockPosition();
        return super.createPath(pPos, pAccuracy);
    }

    public boolean moveTo(Entity pEntity, double pSpeed)
    {
        Path path = this.createPath(pEntity, 0);

        if (path != null)
        {
            return this.moveTo(path, pSpeed);
        }
        else
        {
            this.pathToPosition = pEntity.blockPosition();
            this.speedModifier = pSpeed;
            return true;
        }
    }

    public void tick()
    {
        if (!this.isDone())
        {
            super.tick();
        }
        else
        {
            if (this.pathToPosition != null)
            {
                if (!this.pathToPosition.closerThan(this.mob.position(), (double)this.mob.getBbWidth()) && (!(this.mob.getY() > (double)this.pathToPosition.getY()) || !(new BlockPos((double)this.pathToPosition.getX(), this.mob.getY(), (double)this.pathToPosition.getZ())).closerThan(this.mob.position(), (double)this.mob.getBbWidth())))
                {
                    this.mob.getMoveControl().setWantedPosition((double)this.pathToPosition.getX(), (double)this.pathToPosition.getY(), (double)this.pathToPosition.getZ(), this.speedModifier);
                }
                else
                {
                    this.pathToPosition = null;
                }
            }
        }
    }
}
