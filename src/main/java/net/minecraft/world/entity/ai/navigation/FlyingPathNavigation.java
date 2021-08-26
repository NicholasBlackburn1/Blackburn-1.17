package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation
{
    public FlyingPathNavigation(Mob p_26424_, Level p_26425_)
    {
        super(p_26424_, p_26425_);
    }

    protected PathFinder createPathFinder(int p_26428_)
    {
        this.nodeEvaluator = new FlyNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, p_26428_);
    }

    protected boolean canUpdatePath()
    {
        return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();
    }

    protected Vec3 getTempMobPos()
    {
        return this.mob.position();
    }

    public Path createPath(Entity pEntity, int p_26431_)
    {
        return this.createPath(pEntity.blockPosition(), p_26431_);
    }

    public void tick()
    {
        ++this.tick;

        if (this.hasDelayedRecomputation)
        {
            this.recomputePath();
        }

        if (!this.isDone())
        {
            if (this.canUpdatePath())
            {
                this.followThePath();
            }
            else if (this.path != null && !this.path.isDone())
            {
                Vec3 vec3 = this.path.getNextEntityPos(this.mob);

                if (this.mob.getBlockX() == Mth.floor(vec3.x) && this.mob.getBlockY() == Mth.floor(vec3.y) && this.mob.getBlockZ() == Mth.floor(vec3.z))
                {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);

            if (!this.isDone())
            {
                Vec3 vec31 = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(vec31.x, vec31.y, vec31.z, this.speedModifier);
            }
        }
    }

    protected boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32, int pSizeX, int pSizeY, int pSizeZ)
    {
        int i = Mth.floor(pPosVec31.x);
        int j = Mth.floor(pPosVec31.y);
        int k = Mth.floor(pPosVec31.z);
        double d0 = pPosVec32.x - pPosVec31.x;
        double d1 = pPosVec32.y - pPosVec31.y;
        double d2 = pPosVec32.z - pPosVec31.z;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if (d3 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d4 = 1.0D / Math.sqrt(d3);
            d0 = d0 * d4;
            d1 = d1 * d4;
            d2 = d2 * d4;
            double d5 = 1.0D / Math.abs(d0);
            double d6 = 1.0D / Math.abs(d1);
            double d7 = 1.0D / Math.abs(d2);
            double d8 = (double)i - pPosVec31.x;
            double d9 = (double)j - pPosVec31.y;
            double d10 = (double)k - pPosVec31.z;

            if (d0 >= 0.0D)
            {
                ++d8;
            }

            if (d1 >= 0.0D)
            {
                ++d9;
            }

            if (d2 >= 0.0D)
            {
                ++d10;
            }

            d8 = d8 / d0;
            d9 = d9 / d1;
            d10 = d10 / d2;
            int l = d0 < 0.0D ? -1 : 1;
            int i1 = d1 < 0.0D ? -1 : 1;
            int j1 = d2 < 0.0D ? -1 : 1;
            int k1 = Mth.floor(pPosVec32.x);
            int l1 = Mth.floor(pPosVec32.y);
            int i2 = Mth.floor(pPosVec32.z);
            int j2 = k1 - i;
            int k2 = l1 - j;
            int l2 = i2 - k;

            while (j2 * l > 0 || k2 * i1 > 0 || l2 * j1 > 0)
            {
                if (d8 < d10 && d8 <= d9)
                {
                    d8 += d5;
                    i += l;
                    j2 = k1 - i;
                }
                else if (d9 < d8 && d9 <= d10)
                {
                    d9 += d6;
                    j += i1;
                    k2 = l1 - j;
                }
                else
                {
                    d10 += d7;
                    k += j1;
                    l2 = i2 - k;
                }
            }

            return true;
        }
    }

    public void setCanOpenDoors(boolean pCanOpenDoors)
    {
        this.nodeEvaluator.setCanOpenDoors(pCanOpenDoors);
    }

    public boolean canPassDoors()
    {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean pCanEnterDoors)
    {
        this.nodeEvaluator.setCanPassDoors(pCanEnterDoors);
    }

    public boolean canOpenDoors()
    {
        return this.nodeEvaluator.canPassDoors();
    }

    public boolean isStableDestination(BlockPos pPos)
    {
        return this.level.getBlockState(pPos).entityCanStandOn(this.level, pPos, this.mob);
    }
}
