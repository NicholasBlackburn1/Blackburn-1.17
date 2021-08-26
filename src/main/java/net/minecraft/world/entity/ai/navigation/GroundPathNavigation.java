package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation
{
    private boolean avoidSun;

    public GroundPathNavigation(Mob p_26448_, Level p_26449_)
    {
        super(p_26448_, p_26449_);
    }

    protected PathFinder createPathFinder(int p_26453_)
    {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, p_26453_);
    }

    protected boolean canUpdatePath()
    {
        return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
    }

    protected Vec3 getTempMobPos()
    {
        return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
    }

    public Path createPath(BlockPos pPos, int pAccuracy)
    {
        if (this.level.getBlockState(pPos).isAir())
        {
            BlockPos blockpos;

            for (blockpos = pPos.below(); blockpos.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockpos).isAir(); blockpos = blockpos.below())
            {
            }

            if (blockpos.getY() > this.level.getMinBuildHeight())
            {
                return super.createPath(blockpos.above(), pAccuracy);
            }

            while (blockpos.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir())
            {
                blockpos = blockpos.above();
            }

            pPos = blockpos;
        }

        if (!this.level.getBlockState(pPos).getMaterial().isSolid())
        {
            return super.createPath(pPos, pAccuracy);
        }
        else
        {
            BlockPos blockpos1;

            for (blockpos1 = pPos.above(); blockpos1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).getMaterial().isSolid(); blockpos1 = blockpos1.above())
            {
            }

            return super.createPath(blockpos1, pAccuracy);
        }
    }

    public Path createPath(Entity pPos, int pAccuracy)
    {
        return this.createPath(pPos.blockPosition(), pAccuracy);
    }

    private int getSurfaceY()
    {
        if (this.mob.isInWater() && this.canFloat())
        {
            int i = this.mob.getBlockY();
            BlockState blockstate = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ()));
            int j = 0;

            while (blockstate.is(Blocks.WATER))
            {
                ++i;
                blockstate = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ()));
                ++j;

                if (j > 16)
                {
                    return this.mob.getBlockY();
                }
            }

            return i;
        }
        else
        {
            return Mth.floor(this.mob.getY() + 0.5D);
        }
    }

    protected void trimPath()
    {
        super.trimPath();

        if (this.avoidSun)
        {
            if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())))
            {
                return;
            }

            for (int i = 0; i < this.path.getNodeCount(); ++i)
            {
                Node node = this.path.getNode(i);

                if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z)))
                {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }
    }

    protected boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32, int pSizeX, int pSizeY, int pSizeZ)
    {
        int i = Mth.floor(pPosVec31.x);
        int j = Mth.floor(pPosVec31.z);
        double d0 = pPosVec32.x - pPosVec31.x;
        double d1 = pPosVec32.z - pPosVec31.z;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d3 = 1.0D / Math.sqrt(d2);
            d0 = d0 * d3;
            d1 = d1 * d3;
            pSizeX = pSizeX + 2;
            pSizeZ = pSizeZ + 2;

            if (!this.canWalkOn(i, Mth.floor(pPosVec31.y), j, pSizeX, pSizeY, pSizeZ, pPosVec31, d0, d1))
            {
                return false;
            }
            else
            {
                pSizeX = pSizeX - 2;
                pSizeZ = pSizeZ - 2;
                double d4 = 1.0D / Math.abs(d0);
                double d5 = 1.0D / Math.abs(d1);
                double d6 = (double)i - pPosVec31.x;
                double d7 = (double)j - pPosVec31.z;

                if (d0 >= 0.0D)
                {
                    ++d6;
                }

                if (d1 >= 0.0D)
                {
                    ++d7;
                }

                d6 = d6 / d0;
                d7 = d7 / d1;
                int k = d0 < 0.0D ? -1 : 1;
                int l = d1 < 0.0D ? -1 : 1;
                int i1 = Mth.floor(pPosVec32.x);
                int j1 = Mth.floor(pPosVec32.z);
                int k1 = i1 - i;
                int l1 = j1 - j;

                while (k1 * k > 0 || l1 * l > 0)
                {
                    if (d6 < d7)
                    {
                        d6 += d4;
                        i += k;
                        k1 = i1 - i;
                    }
                    else
                    {
                        d7 += d5;
                        j += l;
                        l1 = j1 - j;
                    }

                    if (!this.canWalkOn(i, Mth.floor(pPosVec31.y), j, pSizeX, pSizeY, pSizeZ, pPosVec31, d0, d1))
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private boolean canWalkOn(int pX, int pY, int pZ, int pSizeX, int pSizeY, int pSizeZ, Vec3 pVec31, double p_26462_, double p_26463_)
    {
        int i = pX - pSizeX / 2;
        int j = pZ - pSizeZ / 2;

        if (!this.canWalkAbove(i, pY, j, pSizeX, pSizeY, pSizeZ, pVec31, p_26462_, p_26463_))
        {
            return false;
        }
        else
        {
            for (int k = i; k < i + pSizeX; ++k)
            {
                for (int l = j; l < j + pSizeZ; ++l)
                {
                    double d0 = (double)k + 0.5D - pVec31.x;
                    double d1 = (double)l + 0.5D - pVec31.z;

                    if (!(d0 * p_26462_ + d1 * p_26463_ < 0.0D))
                    {
                        BlockPathTypes blockpathtypes = this.nodeEvaluator.getBlockPathType(this.level, k, pY - 1, l, this.mob, pSizeX, pSizeY, pSizeZ, true, true);

                        if (!this.hasValidPathType(blockpathtypes))
                        {
                            return false;
                        }

                        blockpathtypes = this.nodeEvaluator.getBlockPathType(this.level, k, pY, l, this.mob, pSizeX, pSizeY, pSizeZ, true, true);
                        float f = this.mob.getPathfindingMalus(blockpathtypes);

                        if (f < 0.0F || f >= 8.0F)
                        {
                            return false;
                        }

                        if (blockpathtypes == BlockPathTypes.DAMAGE_FIRE || blockpathtypes == BlockPathTypes.DANGER_FIRE || blockpathtypes == BlockPathTypes.DAMAGE_OTHER)
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    protected boolean hasValidPathType(BlockPathTypes p_26467_)
    {
        if (p_26467_ == BlockPathTypes.WATER)
        {
            return false;
        }
        else if (p_26467_ == BlockPathTypes.LAVA)
        {
            return false;
        }
        else
        {
            return p_26467_ != BlockPathTypes.OPEN;
        }
    }

    private boolean canWalkAbove(int pX, int pY, int pZ, int pSizeX, int pSizeY, int pSizeZ, Vec3 p_26487_, double p_26488_, double p_26489_)
    {
        for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(pX, pY, pZ), new BlockPos(pX + pSizeX - 1, pY + pSizeY - 1, pZ + pSizeZ - 1)))
        {
            double d0 = (double)blockpos.getX() + 0.5D - p_26487_.x;
            double d1 = (double)blockpos.getZ() + 0.5D - p_26487_.z;

            if (!(d0 * p_26488_ + d1 * p_26489_ < 0.0D) && !this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND))
            {
                return false;
            }
        }

        return true;
    }

    public void setCanOpenDoors(boolean pCanBreakDoors)
    {
        this.nodeEvaluator.setCanOpenDoors(pCanBreakDoors);
    }

    public boolean canPassDoors()
    {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean p_148215_)
    {
        this.nodeEvaluator.setCanPassDoors(p_148215_);
    }

    public boolean canOpenDoors()
    {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean pAvoidSun)
    {
        this.avoidSun = pAvoidSun;
    }
}
