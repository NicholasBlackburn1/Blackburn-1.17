package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult
{
    private final Direction direction;
    private final BlockPos blockPos;
    private final boolean miss;
    private final boolean inside;

    public static BlockHitResult miss(Vec3 pHitVec, Direction pFace, BlockPos pPos)
    {
        return new BlockHitResult(true, pHitVec, pFace, pPos, false);
    }

    public BlockHitResult(Vec3 p_82415_, Direction p_82416_, BlockPos p_82417_, boolean p_82418_)
    {
        this(false, p_82415_, p_82416_, p_82417_, p_82418_);
    }

    private BlockHitResult(boolean p_82420_, Vec3 p_82421_, Direction p_82422_, BlockPos p_82423_, boolean p_82424_)
    {
        super(p_82421_);
        this.miss = p_82420_;
        this.direction = p_82422_;
        this.blockPos = p_82423_;
        this.inside = p_82424_;
    }

    public BlockHitResult withDirection(Direction pNewFace)
    {
        return new BlockHitResult(this.miss, this.location, pNewFace, this.blockPos, this.inside);
    }

    public BlockHitResult withPosition(BlockPos pPos)
    {
        return new BlockHitResult(this.miss, this.location, this.direction, pPos, this.inside);
    }

    public BlockPos getBlockPos()
    {
        return this.blockPos;
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public HitResult.Type getType()
    {
        return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
    }

    public boolean isInside()
    {
        return this.inside;
    }
}
