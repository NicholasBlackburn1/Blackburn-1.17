package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class EndGatewayBlock extends BaseEntityBlock
{
    protected EndGatewayBlock(BlockBehaviour.Properties p_52999_)
    {
        super(p_52999_);
    }

    public BlockEntity newBlockEntity(BlockPos p_153193_, BlockState p_153194_)
    {
        return new TheEndGatewayBlockEntity(p_153193_, p_153194_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153189_, BlockState p_153190_, BlockEntityType<T> p_153191_)
    {
        return createTickerHelper(p_153191_, BlockEntityType.END_GATEWAY, p_153189_.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick);
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof TheEndGatewayBlockEntity)
        {
            int i = ((TheEndGatewayBlockEntity)blockentity).getParticleAmount();

            for (int j = 0; j < i; ++j)
            {
                double d0 = (double)pPos.getX() + pRand.nextDouble();
                double d1 = (double)pPos.getY() + pRand.nextDouble();
                double d2 = (double)pPos.getZ() + pRand.nextDouble();
                double d3 = (pRand.nextDouble() - 0.5D) * 0.5D;
                double d4 = (pRand.nextDouble() - 0.5D) * 0.5D;
                double d5 = (pRand.nextDouble() - 0.5D) * 0.5D;
                int k = pRand.nextInt(2) * 2 - 1;

                if (pRand.nextBoolean())
                {
                    d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
                    d5 = (double)(pRand.nextFloat() * 2.0F * (float)k);
                }
                else
                {
                    d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)k;
                    d3 = (double)(pRand.nextFloat() * 2.0F * (float)k);
                }

                pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
            }
        }
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState)
    {
        return ItemStack.EMPTY;
    }

    public boolean canBeReplaced(BlockState pState, Fluid pFluid)
    {
        return false;
    }
}
