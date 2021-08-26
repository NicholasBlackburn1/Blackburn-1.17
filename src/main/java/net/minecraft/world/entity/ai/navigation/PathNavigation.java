package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation
{
    private static final int MAX_TIME_RECOMPUTE = 20;
    protected final Mob mob;
    protected final Level level;
    @Nullable
    protected Path path;
    protected double speedModifier;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected Vec3i timeoutCachedNode = Vec3i.ZERO;
    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint = 0.5F;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    private BlockPos targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier = 1.0F;
    private final PathFinder pathFinder;
    private boolean isStuck;

    public PathNavigation(Mob p_26515_, Level p_26516_)
    {
        this.mob = p_26515_;
        this.level = p_26516_;
        int i = Mth.floor(p_26515_.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
        this.pathFinder = this.createPathFinder(i);
    }

    public void resetMaxVisitedNodesMultiplier()
    {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(float pMultiplier)
    {
        this.maxVisitedNodesMultiplier = pMultiplier;
    }

    public BlockPos getTargetPos()
    {
        return this.targetPos;
    }

    protected abstract PathFinder createPathFinder(int p_26531_);

    public void setSpeedModifier(double pSpeed)
    {
        this.speedModifier = pSpeed;
    }

    public boolean hasDelayedRecomputation()
    {
        return this.hasDelayedRecomputation;
    }

    public void recomputePath()
    {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L)
        {
            if (this.targetPos != null)
            {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        }
        else
        {
            this.hasDelayedRecomputation = true;
        }
    }

    @Nullable
    public final Path createPath(double pX, double p_26526_, double pY, int p_26528_)
    {
        return this.createPath(new BlockPos(pX, p_26526_, pY), p_26528_);
    }

    @Nullable
    public Path createPath(Stream<BlockPos> pX, int p_26558_)
    {
        return this.createPath(pX.collect(Collectors.toSet()), 8, false, p_26558_);
    }

    @Nullable
    public Path createPath(Set<BlockPos> pX, int p_26550_)
    {
        return this.createPath(pX, 8, false, p_26550_);
    }

    @Nullable
    public Path createPath(BlockPos pX, int p_26547_)
    {
        return this.createPath(ImmutableSet.of(pX), 8, false, p_26547_);
    }

    @Nullable
    public Path createPath(BlockPos pX, int p_148220_, int pY)
    {
        return this.createPath(ImmutableSet.of(pX), 8, false, p_148220_, (float)pY);
    }

    @Nullable
    public Path createPath(Entity pX, int p_26535_)
    {
        return this.createPath(ImmutableSet.of(pX.blockPosition()), 16, true, p_26535_);
    }

    @Nullable
    protected Path createPath(Set<BlockPos> pX, int p_26553_, boolean pY, int p_26555_)
    {
        return this.createPath(pX, p_26553_, pY, p_26555_, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    @Nullable
    protected Path createPath(Set<BlockPos> pX, int p_148224_, boolean pY, int p_148226_, float pZ)
    {
        if (pX.isEmpty())
        {
            return null;
        }
        else if (this.mob.getY() < (double)this.level.getMinBuildHeight())
        {
            return null;
        }
        else if (!this.canUpdatePath())
        {
            return null;
        }
        else if (this.path != null && !this.path.isDone() && pX.contains(this.targetPos))
        {
            return this.path;
        }
        else
        {
            this.level.getProfiler().push("pathfind");
            BlockPos blockpos = pY ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int i = (int)(pZ + (float)p_148224_);
            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
            Path path = this.pathFinder.findPath(pathnavigationregion, this.mob, pX, pZ, p_148226_, this.maxVisitedNodesMultiplier);
            this.level.getProfiler().pop();

            if (path != null && path.getTarget() != null)
            {
                this.targetPos = path.getTarget();
                this.reachRange = p_148226_;
                this.resetStuckTimeout();
            }

            return path;
        }
    }

    public boolean moveTo(double pX, double p_26521_, double pY, double p_26523_)
    {
        return this.moveTo(this.createPath(pX, p_26521_, pY, 1), p_26523_);
    }

    public boolean moveTo(Entity pX, double p_26533_)
    {
        Path path = this.createPath(pX, 1);
        return path != null && this.moveTo(path, p_26533_);
    }

    public boolean moveTo(@Nullable Path pX, double p_26538_)
    {
        if (pX == null)
        {
            this.path = null;
            return false;
        }
        else
        {
            if (!pX.sameAs(this.path))
            {
                this.path = pX;
            }

            if (this.isDone())
            {
                return false;
            }
            else
            {
                this.trimPath();

                if (this.path.getNodeCount() <= 0)
                {
                    return false;
                }
                else
                {
                    this.speedModifier = p_26538_;
                    Vec3 vec3 = this.getTempMobPos();
                    this.lastStuckCheck = this.tick;
                    this.lastStuckCheckPos = vec3;
                    return true;
                }
            }
        }
    }

    @Nullable
    public Path getPath()
    {
        return this.path;
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
                Vec3 vec3 = this.getTempMobPos();
                Vec3 vec31 = this.path.getNextEntityPos(this.mob);

                if (vec3.y > vec31.y && !this.mob.isOnGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z))
                {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);

            if (!this.isDone())
            {
                Vec3 vec32 = this.path.getNextEntityPos(this.mob);
                BlockPos blockpos = new BlockPos(vec32);
                this.mob.getMoveControl().setWantedPosition(vec32.x, this.level.getBlockState(blockpos.below()).isAir() ? vec32.y : WalkNodeEvaluator.getFloorLevel(this.level, blockpos), vec32.z, this.speedModifier);
            }
        }
    }

    protected void followThePath()
    {
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i vec3i = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + 0.5D));
        double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + 0.5D));
        boolean flag = d0 < (double)this.maxDistanceToWaypoint && d2 < (double)this.maxDistanceToWaypoint && d1 < 1.0D;

        if (flag || this.mob.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3))
        {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 p_26560_)
    {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount())
        {
            return false;
        }
        else
        {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());

            if (!p_26560_.closerThan(vec3, 2.0D))
            {
                return false;
            }
            else
            {
                Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 vec32 = vec31.subtract(vec3);
                Vec3 vec33 = p_26560_.subtract(vec3);
                return vec32.dot(vec33) > 0.0D;
            }
        }
    }

    protected void doStuckDetection(Vec3 pPositionVec3)
    {
        if (this.tick - this.lastStuckCheck > 100)
        {
            if (pPositionVec3.distanceToSqr(this.lastStuckCheckPos) < 2.25D)
            {
                this.isStuck = true;
                this.stop();
            }
            else
            {
                this.isStuck = false;
            }

            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = pPositionVec3;
        }

        if (this.path != null && !this.path.isDone())
        {
            Vec3i vec3i = this.path.getNextNodePos();

            if (vec3i.equals(this.timeoutCachedNode))
            {
                this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
            }
            else
            {
                this.timeoutCachedNode = vec3i;
                double d0 = pPositionVec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double)this.mob.getSpeed() * 1000.0D : 0.0D;
            }

            if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D)
            {
                this.timeoutPath();
            }

            this.lastTimeoutCheck = Util.getMillis();
        }
    }

    private void timeoutPath()
    {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout()
    {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0D;
        this.isStuck = false;
    }

    public boolean isDone()
    {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress()
    {
        return !this.isDone();
    }

    public void stop()
    {
        this.path = null;
    }

    protected abstract Vec3 getTempMobPos();

    protected abstract boolean canUpdatePath();

    protected boolean isInLiquid()
    {
        return this.mob.isInWaterOrBubble() || this.mob.isInLava();
    }

    protected void trimPath()
    {
        if (this.path != null)
        {
            for (int i = 0; i < this.path.getNodeCount(); ++i)
            {
                Node node = this.path.getNode(i);
                Node node1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                BlockState blockstate = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));

                if (blockstate.is(BlockTags.CAULDRONS))
                {
                    this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));

                    if (node1 != null && node.y >= node1.y)
                    {
                        this.path.replaceNode(i + 1, node.cloneAndMove(node1.x, node.y + 1, node1.z));
                    }
                }
            }
        }
    }

    protected abstract boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32, int pSizeX, int pSizeY, int pSizeZ);

    public boolean isStableDestination(BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        return this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos);
    }

    public NodeEvaluator getNodeEvaluator()
    {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean pCanSwim)
    {
        this.nodeEvaluator.setCanFloat(pCanSwim);
    }

    public boolean canFloat()
    {
        return this.nodeEvaluator.canFloat();
    }

    public void recomputePath(BlockPos pPos)
    {
        if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0)
        {
            Node node = this.path.getEndNode();
            Vec3 vec3 = new Vec3(((double)node.x + this.mob.getX()) / 2.0D, ((double)node.y + this.mob.getY()) / 2.0D, ((double)node.z + this.mob.getZ()) / 2.0D);

            if (pPos.closerThan(vec3, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex())))
            {
                this.recomputePath();
            }
        }
    }

    public float getMaxDistanceToWaypoint()
    {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck()
    {
        return this.isStuck;
    }
}
