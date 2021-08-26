package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends Block implements EntityBlock, GameMasterBlock
{
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    protected JigsawBlock(BlockBehaviour.Properties p_54225_)
    {
        super(p_54225_);
        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(ORIENTATION);
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        return pState.setValue(ORIENTATION, pRot.rotation().rotate(pState.getValue(ORIENTATION)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        return pState.setValue(ORIENTATION, pMirror.rotation().rotate(pState.getValue(ORIENTATION)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        Direction direction = pContext.getClickedFace();
        Direction direction1;

        if (direction.getAxis() == Direction.Axis.Y)
        {
            direction1 = pContext.getHorizontalDirection().getOpposite();
        }
        else
        {
            direction1 = Direction.UP;
        }

        return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction1));
    }

    public BlockEntity newBlockEntity(BlockPos p_153448_, BlockState p_153449_)
    {
        return new JigsawBlockEntity(p_153448_, p_153449_);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof JigsawBlockEntity && pPlayer.canUseGameMasterBlocks())
        {
            pPlayer.openJigsawBlock((JigsawBlockEntity)blockentity);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    public static boolean canAttach(StructureTemplate.StructureBlockInfo pInfo, StructureTemplate.StructureBlockInfo pInfo2)
    {
        Direction direction = getFrontFacing(pInfo.state);
        Direction direction1 = getFrontFacing(pInfo2.state);
        Direction direction2 = getTopFacing(pInfo.state);
        Direction direction3 = getTopFacing(pInfo2.state);
        JigsawBlockEntity.JointType jigsawblockentity$jointtype = JigsawBlockEntity.JointType.byName(pInfo.nbt.getString("joint")).orElseGet(() ->
        {
            return direction.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
        });
        boolean flag = jigsawblockentity$jointtype == JigsawBlockEntity.JointType.ROLLABLE;
        return direction == direction1.getOpposite() && (flag || direction2 == direction3) && pInfo.nbt.getString("target").equals(pInfo2.nbt.getString("name"));
    }

    public static Direction getFrontFacing(BlockState pState)
    {
        return pState.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState pState)
    {
        return pState.getValue(ORIENTATION).top();
    }
}
