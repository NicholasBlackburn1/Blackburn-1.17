package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock extends Block
{
    public CryingObsidianBlock(BlockBehaviour.Properties p_52371_)
    {
        super(p_52371_);
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pRand.nextInt(5) == 0)
        {
            Direction direction = Direction.getRandom(pRand);

            if (direction != Direction.UP)
            {
                BlockPos blockpos = pPos.relative(direction);
                BlockState blockstate = pLevel.getBlockState(blockpos);

                if (!pState.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, direction.getOpposite()))
                {
                    double d0 = direction.getStepX() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepX() * 0.6D;
                    double d1 = direction.getStepY() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepY() * 0.6D;
                    double d2 = direction.getStepZ() == 0 ? pRand.nextDouble() : 0.5D + (double)direction.getStepZ() * 0.6D;
                    pLevel.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}
