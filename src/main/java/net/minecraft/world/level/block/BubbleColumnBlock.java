package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup
{
    public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
    private static final int CHECK_PERIOD = 5;

    public BubbleColumnBlock(BlockBehaviour.Properties p_50959_)
    {
        super(p_50959_);
        this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        BlockState blockstate = pLevel.getBlockState(pPos.above());

        if (blockstate.isAir())
        {
            pEntity.onAboveBubbleCol(pState.getValue(DRAG_DOWN));

            if (!pLevel.isClientSide)
            {
                ServerLevel serverlevel = (ServerLevel)pLevel;

                for (int i = 0; i < 2; ++i)
                {
                    serverlevel.sendParticles(ParticleTypes.SPLASH, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
                    serverlevel.sendParticles(ParticleTypes.BUBBLE, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.01D, 0.0D, 0.2D);
                }
            }
        }
        else
        {
            pEntity.onInsideBubbleColumn(pState.getValue(DRAG_DOWN));
        }
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        updateColumn(pLevel, pPos, pState, pLevel.getBlockState(pPos.below()));
    }

    public FluidState getFluidState(BlockState pState)
    {
        return Fluids.WATER.getSource(false);
    }

    public static void updateColumn(LevelAccessor p_152708_, BlockPos p_152709_, BlockState p_152710_)
    {
        updateColumn(p_152708_, p_152709_, p_152708_.getBlockState(p_152709_), p_152710_);
    }

    public static void updateColumn(LevelAccessor p_152703_, BlockPos p_152704_, BlockState p_152705_, BlockState p_152706_)
    {
        if (canExistIn(p_152705_))
        {
            BlockState blockstate = getColumnState(p_152706_);
            p_152703_.setBlock(p_152704_, blockstate, 2);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = p_152704_.mutable().move(Direction.UP);

            while (canExistIn(p_152703_.getBlockState(blockpos$mutableblockpos)))
            {
                if (!p_152703_.setBlock(blockpos$mutableblockpos, blockstate, 2))
                {
                    return;
                }

                blockpos$mutableblockpos.move(Direction.UP);
            }
        }
    }

    private static boolean canExistIn(BlockState p_152716_)
    {
        return p_152716_.is(Blocks.BUBBLE_COLUMN) || p_152716_.is(Blocks.WATER) && p_152716_.getFluidState().getAmount() >= 8 && p_152716_.getFluidState().isSource();
    }

    private static BlockState getColumnState(BlockState p_152718_)
    {
        if (p_152718_.is(Blocks.BUBBLE_COLUMN))
        {
            return p_152718_;
        }
        else if (p_152718_.is(Blocks.SOUL_SAND))
        {
            return Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(false));
        }
        else
        {
            return p_152718_.is(Blocks.MAGMA_BLOCK) ? Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(true)) : Blocks.WATER.defaultBlockState();
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        double d0 = (double)pPos.getX();
        double d1 = (double)pPos.getY();
        double d2 = (double)pPos.getZ();

        if (pState.getValue(DRAG_DOWN))
        {
            pLevel.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);

            if (pRand.nextInt(200) == 0)
            {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2F + pRand.nextFloat() * 0.2F, 0.9F + pRand.nextFloat() * 0.15F, false);
            }
        }
        else
        {
            pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
            pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + (double)pRand.nextFloat(), d1 + (double)pRand.nextFloat(), d2 + (double)pRand.nextFloat(), 0.0D, 0.04D, 0.0D);

            if (pRand.nextInt(200) == 0)
            {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2F + pRand.nextFloat() * 0.2F, 0.9F + pRand.nextFloat() * 0.15F, false);
            }
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));

        if (!pState.canSurvive(pLevel, pCurrentPos) || pFacing == Direction.DOWN || pFacing == Direction.UP && !pFacingState.is(Blocks.BUBBLE_COLUMN) && canExistIn(pFacingState))
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 5);
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockState blockstate = pLevel.getBlockState(pPos.below());
        return blockstate.is(Blocks.BUBBLE_COLUMN) || blockstate.is(Blocks.MAGMA_BLOCK) || blockstate.is(Blocks.SOUL_SAND);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return Shapes.empty();
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.INVISIBLE;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(DRAG_DOWN);
    }

    public ItemStack pickupBlock(LevelAccessor p_152712_, BlockPos p_152713_, BlockState p_152714_)
    {
        p_152712_.setBlock(p_152713_, Blocks.AIR.defaultBlockState(), 11);
        return new ItemStack(Items.WATER_BUCKET);
    }

    public Optional<SoundEvent> getPickupSound()
    {
        return Fluids.WATER.getPickupSound();
    }
}
