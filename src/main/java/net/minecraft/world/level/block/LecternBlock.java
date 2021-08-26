package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternBlock extends BaseEntityBlock
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    public static final VoxelShape SHAPE_POST = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.m_83124_(Block.box(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D), Block.box(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D), Block.box(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_NORTH = Shapes.m_83124_(Block.box(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D), Block.box(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D), Block.box(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.m_83124_(Block.box(10.666667D, 10.0D, 0.0D, 15.0D, 14.0D, 16.0D), Block.box(6.333333D, 12.0D, 0.0D, 10.666667D, 16.0D, 16.0D), Block.box(2.0D, 14.0D, 0.0D, 6.333333D, 18.0D, 16.0D), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.m_83124_(Block.box(0.0D, 10.0D, 10.666667D, 16.0D, 14.0D, 15.0D), Block.box(0.0D, 12.0D, 6.333333D, 16.0D, 16.0D, 10.666667D), Block.box(0.0D, 14.0D, 2.0D, 16.0D, 18.0D, 6.333333D), SHAPE_COMMON);
    private static final int PAGE_CHANGE_IMPULSE_TICKS = 2;

    protected LecternBlock(BlockBehaviour.Properties p_54479_)
    {
        super(p_54479_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(false)));
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return SHAPE_COMMON;
    }

    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return true;
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        Level level = pContext.getLevel();
        ItemStack itemstack = pContext.getItemInHand();
        CompoundTag compoundtag = itemstack.getTag();
        Player player = pContext.getPlayer();
        boolean flag = false;

        if (!level.isClientSide && player != null && compoundtag != null && player.canUseGameMasterBlocks() && compoundtag.contains("BlockEntityTag"))
        {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockEntityTag");

            if (compoundtag1.contains("Book"))
            {
                flag = true;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(HAS_BOOK, Boolean.valueOf(flag));
    }

    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE_COLLISION;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        switch ((Direction)pState.getValue(FACING))
        {
            case NORTH:
                return SHAPE_NORTH;

            case SOUTH:
                return SHAPE_SOUTH;

            case EAST:
                return SHAPE_EAST;

            case WEST:
                return SHAPE_WEST;

            default:
                return SHAPE_COMMON;
        }
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(FACING, POWERED, HAS_BOOK);
    }

    public BlockEntity newBlockEntity(BlockPos p_153573_, BlockState p_153574_)
    {
        return new LecternBlockEntity(p_153573_, p_153574_);
    }

    public static boolean tryPlaceBook(@Nullable Player p_153567_, Level p_153568_, BlockPos p_153569_, BlockState p_153570_, ItemStack p_153571_)
    {
        if (!p_153570_.getValue(HAS_BOOK))
        {
            if (!p_153568_.isClientSide)
            {
                placeBook(p_153567_, p_153568_, p_153569_, p_153570_, p_153571_);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    private static void placeBook(@Nullable Player p_153576_, Level p_153577_, BlockPos p_153578_, BlockState p_153579_, ItemStack p_153580_)
    {
        BlockEntity blockentity = p_153577_.getBlockEntity(p_153578_);

        if (blockentity instanceof LecternBlockEntity)
        {
            LecternBlockEntity lecternblockentity = (LecternBlockEntity)blockentity;
            lecternblockentity.setBook(p_153580_.split(1));
            resetBookState(p_153577_, p_153578_, p_153579_, true);
            p_153577_.playSound((Player)null, p_153578_, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            p_153577_.gameEvent(p_153576_, GameEvent.BLOCK_CHANGE, p_153578_);
        }
    }

    public static void resetBookState(Level pLevel, BlockPos pPos, BlockState pState, boolean pHasBook)
    {
        pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(pHasBook)), 3);
        updateBelow(pLevel, pPos, pState);
    }

    public static void signalPageChange(Level pLevel, BlockPos pPos, BlockState pState)
    {
        changePowered(pLevel, pPos, pState, true);
        pLevel.getBlockTicks().scheduleTick(pPos, pState.getBlock(), 2);
        pLevel.levelEvent(1043, pPos, 0);
    }

    private static void changePowered(Level pLevel, BlockPos pPos, BlockState pState, boolean pPowered)
    {
        pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(pPowered)), 3);
        updateBelow(pLevel, pPos, pState);
    }

    private static void updateBelow(Level pLevel, BlockPos pPos, BlockState pState)
    {
        pLevel.updateNeighborsAt(pPos.below(), pState.getBlock());
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        changePowered(pLevel, pPos, pState, false);
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pState.is(pNewState.getBlock()))
        {
            if (pState.getValue(HAS_BOOK))
            {
                this.popBook(pState, pLevel, pPos);
            }

            if (pState.getValue(POWERED))
            {
                pLevel.updateNeighborsAt(pPos.below(), this);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    private void popBook(BlockState pState, Level pLevel, BlockPos pPos)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof LecternBlockEntity)
        {
            LecternBlockEntity lecternblockentity = (LecternBlockEntity)blockentity;
            Direction direction = pState.getValue(FACING);
            ItemStack itemstack = lecternblockentity.getBook().copy();
            float f = 0.25F * (float)direction.getStepX();
            float f1 = 0.25F * (float)direction.getStepZ();
            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D + (double)f, (double)(pPos.getY() + 1), (double)pPos.getZ() + 0.5D + (double)f1, itemstack);
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
            lecternblockentity.clearContent();
        }
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pSide == Direction.UP && pBlockState.getValue(POWERED) ? 15 : 0;
    }

    public boolean hasAnalogOutputSignal(BlockState pState)
    {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos)
    {
        if (pBlockState.getValue(HAS_BOOK))
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof LecternBlockEntity)
            {
                return ((LecternBlockEntity)blockentity).getRedstoneSignal();
            }
        }

        return 0;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pState.getValue(HAS_BOOK))
        {
            if (!pLevel.isClientSide)
            {
                this.openScreen(pLevel, pPos, pPlayer);
            }

            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        else
        {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            return !itemstack.isEmpty() && !itemstack.is(ItemTags.LECTERN_BOOKS) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }

    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos)
    {
        return !pState.getValue(HAS_BOOK) ? null : super.getMenuProvider(pState, pLevel, pPos);
    }

    private void openScreen(Level pLevel, BlockPos pPos, Player pPlayer)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof LecternBlockEntity)
        {
            pPlayer.openMenu((LecternBlockEntity)blockentity);
            pPlayer.awardStat(Stats.INTERACT_WITH_LECTERN);
        }
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }
}
