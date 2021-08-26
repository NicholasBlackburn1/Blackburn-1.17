package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    protected static final int HEIGHT = 9;
    protected static final VoxelShape BASE = Block.box(0.0D, 3.0D, 0.0D, 16.0D, 9.0D, 16.0D);
    private static final int LEG_WIDTH = 3;
    protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D);
    protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 3.0D, 16.0D);
    protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 3.0D, 3.0D);
    protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 3.0D, 16.0D);
    protected static final VoxelShape NORTH_SHAPE = Shapes.m_83124_(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
    protected static final VoxelShape SOUTH_SHAPE = Shapes.m_83124_(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
    protected static final VoxelShape WEST_SHAPE = Shapes.m_83124_(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
    protected static final VoxelShape EAST_SHAPE = Shapes.m_83124_(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
    private final DyeColor color;

    public BedBlock(DyeColor p_49454_, BlockBehaviour.Properties p_49455_)
    {
        super(p_49455_);
        this.color = p_49454_;
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, Boolean.valueOf(false)));
    }

    @Nullable
    public static Direction getBedOrientation(BlockGetter pReader, BlockPos pPos)
    {
        BlockState blockstate = pReader.getBlockState(pPos);
        return blockstate.getBlock() instanceof BedBlock ? blockstate.getValue(FACING) : null;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pLevel.isClientSide)
        {
            return InteractionResult.CONSUME;
        }
        else
        {
            if (pState.getValue(PART) != BedPart.HEAD)
            {
                pPos = pPos.relative(pState.getValue(FACING));
                pState = pLevel.getBlockState(pPos);

                if (!pState.is(this))
                {
                    return InteractionResult.CONSUME;
                }
            }

            if (!canSetSpawn(pLevel))
            {
                pLevel.removeBlock(pPos, false);
                BlockPos blockpos = pPos.relative(pState.getValue(FACING).getOpposite());

                if (pLevel.getBlockState(blockpos).is(this))
                {
                    pLevel.removeBlock(blockpos, false);
                }

                pLevel.explode((Entity)null, DamageSource.badRespawnPointExplosion(), (ExplosionDamageCalculator)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                return InteractionResult.SUCCESS;
            }
            else if (pState.getValue(OCCUPIED))
            {
                if (!this.kickVillagerOutOfBed(pLevel, pPos))
                {
                    pPlayer.displayClientMessage(new TranslatableComponent("block.minecraft.bed.occupied"), true);
                }

                return InteractionResult.SUCCESS;
            }
            else
            {
                pPlayer.startSleepInBed(pPos).ifLeft((p_49477_) ->
                {
                    if (p_49477_ != null)
                    {
                        pPlayer.displayClientMessage(p_49477_.getMessage(), true);
                    }
                });
                return InteractionResult.SUCCESS;
            }
        }
    }

    public static boolean canSetSpawn(Level pLevel)
    {
        return pLevel.dimensionType().bedWorks();
    }

    private boolean kickVillagerOutOfBed(Level pLevel, BlockPos pPos)
    {
        List<Villager> list = pLevel.getEntitiesOfClass(Villager.class, new AABB(pPos), LivingEntity::isSleeping);

        if (list.isEmpty())
        {
            return false;
        }
        else
        {
            list.get(0).stopSleeping();
            return true;
        }
    }

    public void fallOn(Level p_152169_, BlockState p_152170_, BlockPos p_152171_, Entity p_152172_, float p_152173_)
    {
        super.fallOn(p_152169_, p_152170_, p_152171_, p_152172_, p_152173_ * 0.5F);
    }

    public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity)
    {
        if (pEntity.isSuppressingBounce())
        {
            super.updateEntityAfterFallOn(pLevel, pEntity);
        }
        else
        {
            this.bounceUp(pEntity);
        }
    }

    private void bounceUp(Entity pEntity)
    {
        Vec3 vec3 = pEntity.getDeltaMovement();

        if (vec3.y < 0.0D)
        {
            double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
            pEntity.setDeltaMovement(vec3.x, -vec3.y * (double)0.66F * d0, vec3.z);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == getNeighbourDirection(pState.getValue(PART), pState.getValue(FACING)))
        {
            return pFacingState.is(this) && pFacingState.getValue(PART) != pState.getValue(PART) ? pState.setValue(OCCUPIED, pFacingState.getValue(OCCUPIED)) : Blocks.AIR.defaultBlockState();
        }
        else
        {
            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    private static Direction getNeighbourDirection(BedPart pPart, Direction pDirection)
    {
        return pPart == BedPart.FOOT ? pDirection : pDirection.getOpposite();
    }

    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer)
    {
        if (!pLevel.isClientSide && pPlayer.isCreative())
        {
            BedPart bedpart = pState.getValue(PART);

            if (bedpart == BedPart.FOOT)
            {
                BlockPos blockpos = pPos.relative(getNeighbourDirection(bedpart, pState.getValue(FACING)));
                BlockState blockstate = pLevel.getBlockState(blockpos);

                if (blockstate.is(this) && blockstate.getValue(PART) == BedPart.HEAD)
                {
                    pLevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
                }
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        Direction direction = pContext.getHorizontalDirection();
        BlockPos blockpos = pContext.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        return pContext.getLevel().getBlockState(blockpos1).canBeReplaced(pContext) ? this.defaultBlockState().setValue(FACING, direction) : null;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        Direction direction = getConnectedDirection(pState).getOpposite();

        switch (direction)
        {
            case NORTH:
                return NORTH_SHAPE;

            case SOUTH:
                return SOUTH_SHAPE;

            case WEST:
                return WEST_SHAPE;

            default:
                return EAST_SHAPE;
        }
    }

    public static Direction getConnectedDirection(BlockState pState)
    {
        Direction direction = pState.getValue(FACING);
        return pState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState pState)
    {
        BedPart bedpart = pState.getValue(PART);
        return bedpart == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
    }

    private static boolean isBunkBed(BlockGetter pBlockReader, BlockPos pPos)
    {
        return pBlockReader.getBlockState(pPos.below()).getBlock() instanceof BedBlock;
    }

    public static Optional<Vec3> findStandUpPosition(EntityType<?> p_49459_, CollisionGetter p_49460_, BlockPos p_49461_, float p_49462_)
    {
        Direction direction = p_49460_.getBlockState(p_49461_).getValue(FACING);
        Direction direction1 = direction.getClockWise();
        Direction direction2 = direction1.isFacingAngle(p_49462_) ? direction1.getOpposite() : direction1;

        if (isBunkBed(p_49460_, p_49461_))
        {
            return findBunkBedStandUpPosition(p_49459_, p_49460_, p_49461_, direction, direction2);
        }
        else
        {
            int[][] aint = bedStandUpOffsets(direction, direction2);
            Optional<Vec3> optional = m_49469_(p_49459_, p_49460_, p_49461_, aint, true);
            return optional.isPresent() ? optional : m_49469_(p_49459_, p_49460_, p_49461_, aint, false);
        }
    }

    private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> p_49464_, CollisionGetter p_49465_, BlockPos p_49466_, Direction p_49467_, Direction p_49468_)
    {
        int[][] aint = bedSurroundStandUpOffsets(p_49467_, p_49468_);
        Optional<Vec3> optional = m_49469_(p_49464_, p_49465_, p_49466_, aint, true);

        if (optional.isPresent())
        {
            return optional;
        }
        else
        {
            BlockPos blockpos = p_49466_.below();
            Optional<Vec3> optional1 = m_49469_(p_49464_, p_49465_, blockpos, aint, true);

            if (optional1.isPresent())
            {
                return optional1;
            }
            else
            {
                int[][] aint1 = bedAboveStandUpOffsets(p_49467_);
                Optional<Vec3> optional2 = m_49469_(p_49464_, p_49465_, p_49466_, aint1, true);

                if (optional2.isPresent())
                {
                    return optional2;
                }
                else
                {
                    Optional<Vec3> optional3 = m_49469_(p_49464_, p_49465_, p_49466_, aint, false);

                    if (optional3.isPresent())
                    {
                        return optional3;
                    }
                    else
                    {
                        Optional<Vec3> optional4 = m_49469_(p_49464_, p_49465_, blockpos, aint, false);
                        return optional4.isPresent() ? optional4 : m_49469_(p_49464_, p_49465_, p_49466_, aint1, false);
                    }
                }
            }
        }
    }

    private static Optional<Vec3> m_49469_(EntityType<?> p_49470_, CollisionGetter p_49471_, BlockPos p_49472_, int[][] p_49473_, boolean p_49474_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int[] aint : p_49473_)
        {
            blockpos$mutableblockpos.set(p_49472_.getX() + aint[0], p_49472_.getY(), p_49472_.getZ() + aint[1]);
            Vec3 vec3 = DismountHelper.findSafeDismountLocation(p_49470_, p_49471_, blockpos$mutableblockpos, p_49474_);

            if (vec3 != null)
            {
                return Optional.of(vec3);
            }
        }

        return Optional.empty();
    }

    public PushReaction getPistonPushReaction(BlockState pState)
    {
        return PushReaction.DESTROY;
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(FACING, PART, OCCUPIED);
    }

    public BlockEntity newBlockEntity(BlockPos p_152175_, BlockState p_152176_)
    {
        return new BedBlockEntity(p_152175_, p_152176_, this.color);
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
    {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);

        if (!pLevel.isClientSide)
        {
            BlockPos blockpos = pPos.relative(pState.getValue(FACING));
            pLevel.setBlock(blockpos, pState.setValue(PART, BedPart.HEAD), 3);
            pLevel.blockUpdated(pPos, Blocks.AIR);
            pState.updateNeighbourShapes(pLevel, pPos, 3);
        }
    }

    public DyeColor getColor()
    {
        return this.color;
    }

    public long getSeed(BlockState pState, BlockPos pPos)
    {
        BlockPos blockpos = pPos.relative(pState.getValue(FACING), pState.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return Mth.getSeed(blockpos.getX(), pPos.getY(), blockpos.getZ());
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }

    private static int[][] bedStandUpOffsets(Direction p_49539_, Direction p_49540_)
    {
        return ArrayUtils.addAll((int[][])bedSurroundStandUpOffsets(p_49539_, p_49540_), (int[][])bedAboveStandUpOffsets(p_49539_));
    }

    private static int[][] bedSurroundStandUpOffsets(Direction p_49552_, Direction p_49553_)
    {
        return new int[][] {{p_49553_.getStepX(), p_49553_.getStepZ()}, {p_49553_.getStepX() - p_49552_.getStepX(), p_49553_.getStepZ() - p_49552_.getStepZ()}, {p_49553_.getStepX() - p_49552_.getStepX() * 2, p_49553_.getStepZ() - p_49552_.getStepZ() * 2}, { -p_49552_.getStepX() * 2, -p_49552_.getStepZ() * 2}, { -p_49553_.getStepX() - p_49552_.getStepX() * 2, -p_49553_.getStepZ() - p_49552_.getStepZ() * 2}, { -p_49553_.getStepX() - p_49552_.getStepX(), -p_49553_.getStepZ() - p_49552_.getStepZ()}, { -p_49553_.getStepX(), -p_49553_.getStepZ()}, { -p_49553_.getStepX() + p_49552_.getStepX(), -p_49553_.getStepZ() + p_49552_.getStepZ()}, {p_49552_.getStepX(), p_49552_.getStepZ()}, {p_49553_.getStepX() + p_49552_.getStepX(), p_49553_.getStepZ() + p_49552_.getStepZ()}};
    }

    private static int[][] bedAboveStandUpOffsets(Direction p_49537_)
    {
        return new int[][] {{0, 0}, { -p_49537_.getStepX(), -p_49537_.getStepZ()}};
    }
}
