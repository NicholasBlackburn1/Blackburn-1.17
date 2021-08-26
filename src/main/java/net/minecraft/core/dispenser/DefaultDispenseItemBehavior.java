package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior
{
    public final ItemStack dispense(BlockSource p_123391_, ItemStack p_123392_)
    {
        ItemStack itemstack = this.execute(p_123391_, p_123392_);
        this.playSound(p_123391_);
        this.playAnimation(p_123391_, p_123391_.getBlockState().getValue(DispenserBlock.FACING));
        return itemstack;
    }

    protected ItemStack execute(BlockSource pSource, ItemStack pStack)
    {
        Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
        Position position = DispenserBlock.getDispensePosition(pSource);
        ItemStack itemstack = pStack.split(1);
        spawnItem(pSource.getLevel(), itemstack, 6, direction, position);
        return pStack;
    }

    public static void spawnItem(Level pLevel, ItemStack pStack, int pSpeed, Direction pFacing, Position pPosition)
    {
        double d0 = pPosition.x();
        double d1 = pPosition.y();
        double d2 = pPosition.z();

        if (pFacing.getAxis() == Direction.Axis.Y)
        {
            d1 = d1 - 0.125D;
        }
        else
        {
            d1 = d1 - 0.15625D;
        }

        ItemEntity itementity = new ItemEntity(pLevel, d0, d1, d2, pStack);
        double d3 = pLevel.random.nextDouble() * 0.1D + 0.2D;
        itementity.setDeltaMovement(pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)pFacing.getStepX() * d3, pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)0.2F, pLevel.random.nextGaussian() * (double)0.0075F * (double)pSpeed + (double)pFacing.getStepZ() * d3);
        pLevel.addFreshEntity(itementity);
    }

    protected void playSound(BlockSource pSource)
    {
        pSource.getLevel().levelEvent(1000, pSource.getPos(), 0);
    }

    protected void playAnimation(BlockSource pSource, Direction pFacing)
    {
        pSource.getLevel().levelEvent(2000, pSource.getPos(), pFacing.get3DDataValue());
    }
}
