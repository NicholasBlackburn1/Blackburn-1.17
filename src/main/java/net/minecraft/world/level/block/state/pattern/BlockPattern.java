package net.minecraft.world.level.block.state.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

public class BlockPattern
{
    private final Predicate<BlockInWorld>[][][] pattern;
    private final int depth;
    private final int height;
    private final int width;

    public BlockPattern(Predicate<BlockInWorld>[][][] p_61182_)
    {
        this.pattern = p_61182_;
        this.depth = p_61182_.length;

        if (this.depth > 0)
        {
            this.height = p_61182_[0].length;

            if (this.height > 0)
            {
                this.width = p_61182_[0][0].length;
            }
            else
            {
                this.width = 0;
            }
        }
        else
        {
            this.height = 0;
            this.width = 0;
        }
    }

    public int getDepth()
    {
        return this.depth;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getWidth()
    {
        return this.width;
    }

    @VisibleForTesting
    public Predicate<BlockInWorld>[][][] getPattern()
    {
        return this.pattern;
    }

    @Nullable
    @VisibleForTesting
    public BlockPattern.BlockPatternMatch matches(LevelReader pPos, BlockPos pFinger, Direction pThumb, Direction pLcache)
    {
        LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(pPos, false);
        return this.matches(pFinger, pThumb, pLcache, loadingcache);
    }

    @Nullable
    private BlockPattern.BlockPatternMatch matches(BlockPos pPos, Direction pFinger, Direction pThumb, LoadingCache<BlockPos, BlockInWorld> pLcache)
    {
        for (int i = 0; i < this.width; ++i)
        {
            for (int j = 0; j < this.height; ++j)
            {
                for (int k = 0; k < this.depth; ++k)
                {
                    if (!this.pattern[k][j][i].test(pLcache.getUnchecked(translateAndRotate(pPos, pFinger, pThumb, i, j, k))))
                    {
                        return null;
                    }
                }
            }
        }

        return new BlockPattern.BlockPatternMatch(pPos, pFinger, pThumb, pLcache, this.width, this.height, this.depth);
    }

    @Nullable
    public BlockPattern.BlockPatternMatch find(LevelReader pLevel, BlockPos pPos)
    {
        LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(pLevel, false);
        int i = Math.max(Math.max(this.width, this.height), this.depth);

        for (BlockPos blockpos : BlockPos.betweenClosed(pPos, pPos.offset(i - 1, i - 1, i - 1)))
        {
            for (Direction direction : Direction.values())
            {
                for (Direction direction1 : Direction.values())
                {
                    if (direction1 != direction && direction1 != direction.getOpposite())
                    {
                        BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = this.matches(blockpos, direction, direction1, loadingcache);

                        if (blockpattern$blockpatternmatch != null)
                        {
                            return blockpattern$blockpatternmatch;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader pLevel, boolean pForceLoad)
    {
        return CacheBuilder.newBuilder().build(new BlockPattern.BlockCacheLoader(pLevel, pForceLoad));
    }

    protected static BlockPos translateAndRotate(BlockPos pPos, Direction pFinger, Direction pThumb, int pPalmOffset, int pThumbOffset, int pFingerOffset)
    {
        if (pFinger != pThumb && pFinger != pThumb.getOpposite())
        {
            Vec3i vec3i = new Vec3i(pFinger.getStepX(), pFinger.getStepY(), pFinger.getStepZ());
            Vec3i vec3i1 = new Vec3i(pThumb.getStepX(), pThumb.getStepY(), pThumb.getStepZ());
            Vec3i vec3i2 = vec3i.cross(vec3i1);
            return pPos.offset(vec3i1.getX() * -pThumbOffset + vec3i2.getX() * pPalmOffset + vec3i.getX() * pFingerOffset, vec3i1.getY() * -pThumbOffset + vec3i2.getY() * pPalmOffset + vec3i.getY() * pFingerOffset, vec3i1.getZ() * -pThumbOffset + vec3i2.getZ() * pPalmOffset + vec3i.getZ() * pFingerOffset);
        }
        else
        {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
    }

    static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld>
    {
        private final LevelReader level;
        private final boolean loadChunks;

        public BlockCacheLoader(LevelReader p_61207_, boolean p_61208_)
        {
            this.level = p_61207_;
            this.loadChunks = p_61208_;
        }

        public BlockInWorld load(BlockPos p_61210_)
        {
            return new BlockInWorld(this.level, p_61210_, this.loadChunks);
        }
    }

    public static class BlockPatternMatch
    {
        private final BlockPos frontTopLeft;
        private final Direction forwards;
        private final Direction up;
        private final LoadingCache<BlockPos, BlockInWorld> cache;
        private final int width;
        private final int height;
        private final int depth;

        public BlockPatternMatch(BlockPos p_61221_, Direction p_61222_, Direction p_61223_, LoadingCache<BlockPos, BlockInWorld> p_61224_, int p_61225_, int p_61226_, int p_61227_)
        {
            this.frontTopLeft = p_61221_;
            this.forwards = p_61222_;
            this.up = p_61223_;
            this.cache = p_61224_;
            this.width = p_61225_;
            this.height = p_61226_;
            this.depth = p_61227_;
        }

        public BlockPos getFrontTopLeft()
        {
            return this.frontTopLeft;
        }

        public Direction getForwards()
        {
            return this.forwards;
        }

        public Direction getUp()
        {
            return this.up;
        }

        public int getWidth()
        {
            return this.width;
        }

        public int getHeight()
        {
            return this.height;
        }

        public int getDepth()
        {
            return this.depth;
        }

        public BlockInWorld getBlock(int pPalmOffset, int pThumbOffset, int pFingerOffset)
        {
            return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), pPalmOffset, pThumbOffset, pFingerOffset));
        }

        public String toString()
        {
            return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
        }
    }
}
