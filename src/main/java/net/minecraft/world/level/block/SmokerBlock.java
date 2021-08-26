package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlock extends AbstractFurnaceBlock
{
    protected SmokerBlock(BlockBehaviour.Properties p_56439_)
    {
        super(p_56439_);
    }

    public BlockEntity newBlockEntity(BlockPos p_154644_, BlockState p_154645_)
    {
        return new SmokerBlockEntity(p_154644_, p_154645_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_154640_, BlockState p_154641_, BlockEntityType<T> p_154642_)
    {
        return createFurnaceTicker(p_154640_, p_154642_, BlockEntityType.SMOKER);
    }

    protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof SmokerBlockEntity)
        {
            pPlayer.openMenu((MenuProvider)blockentity);
            pPlayer.awardStat(Stats.INTERACT_WITH_SMOKER);
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pState.getValue(LIT))
        {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ() + 0.5D;

            if (pRand.nextDouble() < 0.1D)
            {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            pLevel.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
