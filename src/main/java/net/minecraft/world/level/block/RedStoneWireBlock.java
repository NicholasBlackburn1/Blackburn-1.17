package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Vector3f;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block
{
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
    protected static final int H = 1;
    protected static final int W = 3;
    protected static final int E = 13;
    protected static final int N = 3;
    protected static final int S = 13;
    private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
    private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    private static final Vec3[] COLORS = Util.make(new Vec3[16], (p_154319_) ->
    {
        for (int i = 0; i <= 15; ++i)
        {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            p_154319_[i] = new Vec3((double)f1, (double)f2, (double)f3);
        }
    });
    private static final float PARTICLE_DENSITY = 0.2F;
    private final BlockState crossState;
    private boolean shouldSignal = true;

    public RedStoneWireBlock(BlockBehaviour.Properties p_55511_)
    {
        super(p_55511_);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
        this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

        for (BlockState blockstate : this.getStateDefinition().getPossibleStates())
        {
            if (blockstate.getValue(POWER) == 0)
            {
                SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
            }
        }
    }

    private VoxelShape calculateShape(BlockState pState)
    {
        VoxelShape voxelshape = SHAPE_DOT;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));

            if (redstoneside == RedstoneSide.SIDE)
            {
                voxelshape = Shapes.or(voxelshape, SHAPES_FLOOR.get(direction));
            }
            else if (redstoneside == RedstoneSide.UP)
            {
                voxelshape = Shapes.or(voxelshape, SHAPES_UP.get(direction));
            }
        }

        return voxelshape;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPES_CACHE.get(pState.setValue(POWER, Integer.valueOf(0)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return this.getConnectionState(pContext.getLevel(), this.crossState, pContext.getClickedPos());
    }

    private BlockState getConnectionState(BlockGetter pReader, BlockState pState, BlockPos pPos)
    {
        boolean flag = isDot(pState);
        pState = this.getMissingConnections(pReader, this.defaultBlockState().setValue(POWER, pState.getValue(POWER)), pPos);

        if (flag && isDot(pState))
        {
            return pState;
        }
        else
        {
            boolean flag1 = pState.getValue(NORTH).isConnected();
            boolean flag2 = pState.getValue(SOUTH).isConnected();
            boolean flag3 = pState.getValue(EAST).isConnected();
            boolean flag4 = pState.getValue(WEST).isConnected();
            boolean flag5 = !flag1 && !flag2;
            boolean flag6 = !flag3 && !flag4;

            if (!flag4 && flag5)
            {
                pState = pState.setValue(WEST, RedstoneSide.SIDE);
            }

            if (!flag3 && flag5)
            {
                pState = pState.setValue(EAST, RedstoneSide.SIDE);
            }

            if (!flag1 && flag6)
            {
                pState = pState.setValue(NORTH, RedstoneSide.SIDE);
            }

            if (!flag2 && flag6)
            {
                pState = pState.setValue(SOUTH, RedstoneSide.SIDE);
            }

            return pState;
        }
    }

    private BlockState getMissingConnections(BlockGetter pReader, BlockState pState, BlockPos pPos)
    {
        boolean flag = !pReader.getBlockState(pPos.above()).isRedstoneConductor(pReader, pPos);

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (!pState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected())
            {
                RedstoneSide redstoneside = this.getConnectingSide(pReader, pPos, direction, flag);
                pState = pState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
            }
        }

        return pState;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == Direction.DOWN)
        {
            return pState;
        }
        else if (pFacing == Direction.UP)
        {
            return this.getConnectionState(pLevel, pState, pCurrentPos);
        }
        else
        {
            RedstoneSide redstoneside = this.getConnectingSide(pLevel, pCurrentPos, pFacing);
            return redstoneside.isConnected() == pState.getValue(PROPERTY_BY_DIRECTION.get(pFacing)).isConnected() && !isCross(pState) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside) : this.getConnectionState(pLevel, this.crossState.setValue(POWER, pState.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside), pCurrentPos);
        }
    }

    private static boolean isCross(BlockState pState)
    {
        return pState.getValue(NORTH).isConnected() && pState.getValue(SOUTH).isConnected() && pState.getValue(EAST).isConnected() && pState.getValue(WEST).isConnected();
    }

    private static boolean isDot(BlockState pState)
    {
        return !pState.getValue(NORTH).isConnected() && !pState.getValue(SOUTH).isConnected() && !pState.getValue(EAST).isConnected() && !pState.getValue(WEST).isConnected();
    }

    public void updateIndirectNeighbourShapes(BlockState pState, LevelAccessor pLevel, BlockPos pPos, int pFlags, int pRecursionLeft)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));

            if (redstoneside != RedstoneSide.NONE && !pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(pPos, direction)).is(this))
            {
                blockpos$mutableblockpos.move(Direction.DOWN);
                BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);

                if (!blockstate.is(Blocks.OBSERVER))
                {
                    BlockPos blockpos = blockpos$mutableblockpos.relative(direction.getOpposite());
                    BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos), pLevel, blockpos$mutableblockpos, blockpos);
                    updateOrDestroy(blockstate, blockstate1, pLevel, blockpos$mutableblockpos, pFlags, pRecursionLeft);
                }

                blockpos$mutableblockpos.setWithOffset(pPos, direction).move(Direction.UP);
                BlockState blockstate3 = pLevel.getBlockState(blockpos$mutableblockpos);

                if (!blockstate3.is(Blocks.OBSERVER))
                {
                    BlockPos blockpos1 = blockpos$mutableblockpos.relative(direction.getOpposite());
                    BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos1), pLevel, blockpos$mutableblockpos, blockpos1);
                    updateOrDestroy(blockstate3, blockstate2, pLevel, blockpos$mutableblockpos, pFlags, pRecursionLeft);
                }
            }
        }
    }

    private RedstoneSide getConnectingSide(BlockGetter pLevel, BlockPos pPos, Direction pFace)
    {
        return this.getConnectingSide(pLevel, pPos, pFace, !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos));
    }

    private RedstoneSide getConnectingSide(BlockGetter pLevel, BlockPos pPos, Direction pFace, boolean p_55526_)
    {
        BlockPos blockpos = pPos.relative(pFace);
        BlockState blockstate = pLevel.getBlockState(blockpos);

        if (p_55526_)
        {
            boolean flag = this.canSurviveOn(pLevel, blockpos, blockstate);

            if (flag && shouldConnectTo(pLevel.getBlockState(blockpos.above())))
            {
                if (blockstate.isFaceSturdy(pLevel, blockpos, pFace.getOpposite()))
                {
                    return RedstoneSide.UP;
                }

                return RedstoneSide.SIDE;
            }
        }

        return !shouldConnectTo(blockstate, pFace) && (blockstate.isRedstoneConductor(pLevel, blockpos) || !shouldConnectTo(pLevel.getBlockState(blockpos.below()))) ? RedstoneSide.NONE : RedstoneSide.SIDE;
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return this.canSurviveOn(pLevel, blockpos, blockstate);
    }

    private boolean canSurviveOn(BlockGetter pReader, BlockPos pPos, BlockState pState)
    {
        return pState.isFaceSturdy(pReader, pPos, Direction.UP) || pState.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(Level pLevel, BlockPos pPos, BlockState pState)
    {
        int i = this.calculateTargetStrength(pLevel, pPos);

        if (pState.getValue(POWER) != i)
        {
            if (pLevel.getBlockState(pPos) == pState)
            {
                pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pPos);

            for (Direction direction : Direction.values())
            {
                set.add(pPos.relative(direction));
            }

            for (BlockPos blockpos : set)
            {
                pLevel.updateNeighborsAt(blockpos, this);
            }
        }
    }

    private int calculateTargetStrength(Level pLevel, BlockPos pPos)
    {
        this.shouldSignal = false;
        int i = pLevel.getBestNeighborSignal(pPos);
        this.shouldSignal = true;
        int j = 0;

        if (i < 15)
        {
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                BlockPos blockpos = pPos.relative(direction);
                BlockState blockstate = pLevel.getBlockState(blockpos);
                j = Math.max(j, this.getWireSignal(blockstate));
                BlockPos blockpos1 = pPos.above();

                if (blockstate.isRedstoneConductor(pLevel, blockpos) && !pLevel.getBlockState(blockpos1).isRedstoneConductor(pLevel, blockpos1))
                {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.above())));
                }
                else if (!blockstate.isRedstoneConductor(pLevel, blockpos))
                {
                    j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.below())));
                }
            }
        }

        return Math.max(i, j - 1);
    }

    private int getWireSignal(BlockState pState)
    {
        return pState.is(this) ? pState.getValue(POWER) : 0;
    }

    private void checkCornerChangeAt(Level pLevel, BlockPos pPos)
    {
        if (pLevel.getBlockState(pPos).is(this))
        {
            pLevel.updateNeighborsAt(pPos, this);

            for (Direction direction : Direction.values())
            {
                pLevel.updateNeighborsAt(pPos.relative(direction), this);
            }
        }
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        if (!pOldState.is(pState.getBlock()) && !pLevel.isClientSide)
        {
            this.updatePowerStrength(pLevel, pPos, pState);

            for (Direction direction : Direction.Plane.VERTICAL)
            {
                pLevel.updateNeighborsAt(pPos.relative(direction), this);
            }

            this.updateNeighborsOfNeighboringWires(pLevel, pPos);
        }
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pIsMoving && !pState.is(pNewState.getBlock()))
        {
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

            if (!pLevel.isClientSide)
            {
                for (Direction direction : Direction.values())
                {
                    pLevel.updateNeighborsAt(pPos.relative(direction), this);
                }

                this.updatePowerStrength(pLevel, pPos, pState);
                this.updateNeighborsOfNeighboringWires(pLevel, pPos);
            }
        }
    }

    private void updateNeighborsOfNeighboringWires(Level pLevel, BlockPos pPos)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            this.checkCornerChangeAt(pLevel, pPos.relative(direction));
        }

        for (Direction direction1 : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction1);

            if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos))
            {
                this.checkCornerChangeAt(pLevel, blockpos.above());
            }
            else
            {
                this.checkCornerChangeAt(pLevel, blockpos.below());
            }
        }
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        if (!pLevel.isClientSide)
        {
            if (pState.canSurvive(pLevel, pPos))
            {
                this.updatePowerStrength(pLevel, pPos, pState);
            }
            else
            {
                dropResources(pState, pLevel, pPos);
                pLevel.removeBlock(pPos, false);
            }
        }
    }

    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return !this.shouldSignal ? 0 : pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        if (this.shouldSignal && pSide != Direction.DOWN)
        {
            int i = pBlockState.getValue(POWER);

            if (i == 0)
            {
                return 0;
            }
            else
            {
                return pSide != Direction.UP && !this.getConnectionState(pBlockAccess, pBlockState, pPos).getValue(PROPERTY_BY_DIRECTION.get(pSide.getOpposite())).isConnected() ? 0 : i;
            }
        }
        else
        {
            return 0;
        }
    }

    protected static boolean shouldConnectTo(BlockState pState)
    {
        return shouldConnectTo(pState, (Direction)null);
    }

    protected static boolean shouldConnectTo(BlockState pState, @Nullable Direction p_55596_)
    {
        if (pState.is(Blocks.REDSTONE_WIRE))
        {
            return true;
        }
        else if (pState.is(Blocks.REPEATER))
        {
            Direction direction = pState.getValue(RepeaterBlock.FACING);
            return direction == p_55596_ || direction.getOpposite() == p_55596_;
        }
        else if (pState.is(Blocks.OBSERVER))
        {
            return p_55596_ == pState.getValue(ObserverBlock.FACING);
        }
        else
        {
            return pState.isSignalSource() && p_55596_ != null;
        }
    }

    public boolean isSignalSource(BlockState pState)
    {
        return this.shouldSignal;
    }

    public static int getColorForPower(int pPower)
    {
        Vec3 vec3 = COLORS[pPower];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    private void spawnParticlesAlongLine(Level p_154310_, Random p_154311_, BlockPos p_154312_, Vec3 p_154313_, Direction p_154314_, Direction p_154315_, float p_154316_, float p_154317_)
    {
        float f = p_154317_ - p_154316_;

        if (!(p_154311_.nextFloat() >= 0.2F * f))
        {
            float f1 = 0.4375F;
            float f2 = p_154316_ + f * p_154311_.nextFloat();
            double d0 = 0.5D + (double)(0.4375F * (float)p_154314_.getStepX()) + (double)(f2 * (float)p_154315_.getStepX());
            double d1 = 0.5D + (double)(0.4375F * (float)p_154314_.getStepY()) + (double)(f2 * (float)p_154315_.getStepY());
            double d2 = 0.5D + (double)(0.4375F * (float)p_154314_.getStepZ()) + (double)(f2 * (float)p_154315_.getStepZ());
            p_154310_.addParticle(new DustParticleOptions(new Vector3f(p_154313_), 1.0F), (double)p_154312_.getX() + d0, (double)p_154312_.getY() + d1, (double)p_154312_.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        int i = pState.getValue(POWER);

        if (i != 0)
        {
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));

                switch (redstoneside)
                {
                    case UP:
                        this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);

                    case SIDE:
                        this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                        break;

                    case NONE:
                    default:
                        this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }
        }
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        switch (pRot)
        {
            case CLOCKWISE_180:
                return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(EAST, pState.getValue(WEST)).setValue(SOUTH, pState.getValue(NORTH)).setValue(WEST, pState.getValue(EAST));

            case COUNTERCLOCKWISE_90:
                return pState.setValue(NORTH, pState.getValue(EAST)).setValue(EAST, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(WEST)).setValue(WEST, pState.getValue(NORTH));

            case CLOCKWISE_90:
                return pState.setValue(NORTH, pState.getValue(WEST)).setValue(EAST, pState.getValue(NORTH)).setValue(SOUTH, pState.getValue(EAST)).setValue(WEST, pState.getValue(SOUTH));

            default:
                return pState;
        }
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        switch (pMirror)
        {
            case LEFT_RIGHT:
                return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(NORTH));

            case FRONT_BACK:
                return pState.setValue(EAST, pState.getValue(WEST)).setValue(WEST, pState.getValue(EAST));

            default:
                return super.mirror(pState, pMirror);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(NORTH, EAST, SOUTH, WEST, POWER);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (!pPlayer.getAbilities().mayBuild)
        {
            return InteractionResult.PASS;
        }
        else
        {
            if (isCross(pState) || isDot(pState))
            {
                BlockState blockstate = isCross(pState) ? this.defaultBlockState() : this.crossState;
                blockstate = blockstate.setValue(POWER, pState.getValue(POWER));
                blockstate = this.getConnectionState(pLevel, blockstate, pPos);

                if (blockstate != pState)
                {
                    pLevel.setBlock(pPos, blockstate, 3);
                    this.updatesOnShapeChange(pLevel, pPos, pState, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private void updatesOnShapeChange(Level pLevel, BlockPos pPos, BlockState pPrevState, BlockState pNewState)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction);

            if (pPrevState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != pNewState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos))
            {
                pLevel.updateNeighborsAtExceptFromFacing(blockpos, pNewState.getBlock(), direction.getOpposite());
            }
        }
    }
}
