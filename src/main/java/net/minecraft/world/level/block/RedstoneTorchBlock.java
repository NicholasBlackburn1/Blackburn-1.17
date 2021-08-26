package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class RedstoneTorchBlock extends TorchBlock
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap<>();
    public static final int RECENT_TOGGLE_TIMER = 60;
    public static final int MAX_RECENT_TOGGLES = 8;
    public static final int RESTART_DELAY = 160;
    private static final int TOGGLE_DELAY = 2;

    protected RedstoneTorchBlock(BlockBehaviour.Properties p_55678_)
    {
        super(p_55678_, DustParticleOptions.REDSTONE);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        for (Direction direction : Direction.values())
        {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
        }
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pIsMoving)
        {
            for (Direction direction : Direction.values())
            {
                pLevel.updateNeighborsAt(pPos.relative(direction), this);
            }
        }
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pBlockState.getValue(LIT) && Direction.UP != pSide ? 15 : 0;
    }

    protected boolean hasNeighborSignal(Level pLevel, BlockPos pPos, BlockState pState)
    {
        return pLevel.hasSignal(pPos.below(), Direction.DOWN);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        boolean flag = this.hasNeighborSignal(pLevel, pPos, pState);
        List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.get(pLevel);

        while (list != null && !list.isEmpty() && pLevel.getGameTime() - (list.get(0)).when > 60L)
        {
            list.remove(0);
        }

        if (pState.getValue(LIT))
        {
            if (flag)
            {
                pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(false)), 3);

                if (isToggledTooFrequently(pLevel, pPos, true))
                {
                    pLevel.levelEvent(1502, pPos, 0);
                    pLevel.getBlockTicks().scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 160);
                }
            }
        }
        else if (!flag && !isToggledTooFrequently(pLevel, pPos, false))
        {
            pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(true)), 3);
        }
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        if (pState.getValue(LIT) == this.hasNeighborSignal(pLevel, pPos, pState) && !pLevel.getBlockTicks().willTickThisTick(pPos, this))
        {
            pLevel.getBlockTicks().scheduleTick(pPos, this, 2);
        }
    }

    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pSide == Direction.DOWN ? pBlockState.getSignal(pBlockAccess, pPos, pSide) : 0;
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pState.getValue(LIT))
        {
            double d0 = (double)pPos.getX() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double)pPos.getY() + 0.7D + (pRand.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double)pPos.getZ() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
            pLevel.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(LIT);
    }

    private static boolean isToggledTooFrequently(Level pLevel, BlockPos pLevelConflicting, boolean pPos)
    {
        List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.computeIfAbsent(pLevel, (p_55680_) ->
        {
            return Lists.newArrayList();
        });

        if (pPos)
        {
            list.add(new RedstoneTorchBlock.Toggle(pLevelConflicting.immutable(), pLevel.getGameTime()));
        }

        int i = 0;

        for (int j = 0; j < list.size(); ++j)
        {
            RedstoneTorchBlock.Toggle redstonetorchblock$toggle = list.get(j);

            if (redstonetorchblock$toggle.pos.equals(pLevelConflicting))
            {
                ++i;

                if (i >= 8)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static class Toggle
    {
        final BlockPos pos;
        final long when;

        public Toggle(BlockPos p_55734_, long p_55735_)
        {
            this.pos = p_55734_;
            this.when = p_55735_;
        }
    }
}
