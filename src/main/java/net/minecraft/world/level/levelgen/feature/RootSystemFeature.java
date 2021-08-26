package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature extends Feature<RootSystemConfiguration>
{
    public RootSystemFeature(Codec<RootSystemConfiguration> p_160218_)
    {
        super(p_160218_);
    }

    public boolean place(FeaturePlaceContext<RootSystemConfiguration> p_160257_)
    {
        WorldGenLevel worldgenlevel = p_160257_.level();
        BlockPos blockpos = p_160257_.origin();

        if (!worldgenlevel.getBlockState(blockpos).isAir())
        {
            return false;
        }
        else
        {
            Random random = p_160257_.random();
            BlockPos blockpos1 = p_160257_.origin();
            RootSystemConfiguration rootsystemconfiguration = p_160257_.config();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos1.mutable();

            if (this.placeDirtAndTree(worldgenlevel, p_160257_.chunkGenerator(), rootsystemconfiguration, random, blockpos$mutableblockpos, blockpos1))
            {
                this.placeRoots(worldgenlevel, rootsystemconfiguration, random, blockpos1, blockpos$mutableblockpos);
            }

            return true;
        }
    }

    private boolean spaceForTree(WorldGenLevel p_160236_, RootSystemConfiguration p_160237_, BlockPos p_160238_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = p_160238_.mutable();

        for (int i = 1; i <= p_160237_.requiredVerticalSpaceForTree; ++i)
        {
            blockpos$mutableblockpos.move(Direction.UP);
            BlockState blockstate = p_160236_.getBlockState(blockpos$mutableblockpos);

            if (!isAllowedTreeSpace(blockstate, i, p_160237_.allowedVerticalWaterForTree))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState p_160253_, int p_160254_, int p_160255_)
    {
        return p_160253_.isAir() || p_160254_ <= p_160255_ && p_160253_.getFluidState().is(FluidTags.WATER);
    }

    private boolean placeDirtAndTree(WorldGenLevel p_160223_, ChunkGenerator p_160224_, RootSystemConfiguration p_160225_, Random p_160226_, BlockPos.MutableBlockPos p_160227_, BlockPos p_160228_)
    {
        int i = p_160228_.getX();
        int j = p_160228_.getZ();

        for (int k = 0; k < p_160225_.rootColumnMaxHeight; ++k)
        {
            p_160227_.move(Direction.UP);

            if (TreeFeature.validTreePos(p_160223_, p_160227_))
            {
                if (this.spaceForTree(p_160223_, p_160225_, p_160227_))
                {
                    BlockPos blockpos = p_160227_.below();

                    if (p_160223_.getFluidState(blockpos).is(FluidTags.LAVA) || !p_160223_.getBlockState(blockpos).getMaterial().isSolid())
                    {
                        return false;
                    }

                    if (this.tryPlaceAzaleaTree(p_160223_, p_160224_, p_160225_, p_160226_, p_160227_))
                    {
                        return true;
                    }
                }
            }
            else
            {
                this.placeRootedDirt(p_160223_, p_160225_, p_160226_, i, j, p_160227_);
            }
        }

        return false;
    }

    private boolean tryPlaceAzaleaTree(WorldGenLevel p_160230_, ChunkGenerator p_160231_, RootSystemConfiguration p_160232_, Random p_160233_, BlockPos p_160234_)
    {
        return p_160232_.treeFeature.get().place(p_160230_, p_160231_, p_160233_, p_160234_);
    }

    private void placeRootedDirt(WorldGenLevel p_160240_, RootSystemConfiguration p_160241_, Random p_160242_, int p_160243_, int p_160244_, BlockPos.MutableBlockPos p_160245_)
    {
        int i = p_160241_.rootRadius;
        Tag<Block> tag = BlockTags.getAllTags().getTag(p_160241_.rootReplaceable);
        Predicate<BlockState> predicate = tag == null ? (p_160259_) ->
        {
            return true;
        } : (p_160221_) ->
        {
            return p_160221_.is(tag);
        };

        for (int j = 0; j < p_160241_.rootPlacementAttempts; ++j)
        {
            p_160245_.setWithOffset(p_160245_, p_160242_.nextInt(i) - p_160242_.nextInt(i), 0, p_160242_.nextInt(i) - p_160242_.nextInt(i));

            if (predicate.test(p_160240_.getBlockState(p_160245_)))
            {
                p_160240_.setBlock(p_160245_, p_160241_.rootStateProvider.getState(p_160242_, p_160245_), 2);
            }

            p_160245_.setX(p_160243_);
            p_160245_.setZ(p_160244_);
        }
    }

    private void placeRoots(WorldGenLevel p_160247_, RootSystemConfiguration p_160248_, Random p_160249_, BlockPos p_160250_, BlockPos.MutableBlockPos p_160251_)
    {
        int i = p_160248_.hangingRootRadius;
        int j = p_160248_.hangingRootsVerticalSpan;

        for (int k = 0; k < p_160248_.hangingRootPlacementAttempts; ++k)
        {
            p_160251_.setWithOffset(p_160250_, p_160249_.nextInt(i) - p_160249_.nextInt(i), p_160249_.nextInt(j) - p_160249_.nextInt(j), p_160249_.nextInt(i) - p_160249_.nextInt(i));

            if (p_160247_.isEmptyBlock(p_160251_))
            {
                BlockState blockstate = p_160248_.hangingRootStateProvider.getState(p_160249_, p_160251_);

                if (blockstate.canSurvive(p_160247_, p_160251_) && p_160247_.getBlockState(p_160251_.above()).isFaceSturdy(p_160247_, p_160251_, Direction.DOWN))
                {
                    p_160247_.setBlock(p_160251_, blockstate, 2);
                }
            }
        }
    }
}
