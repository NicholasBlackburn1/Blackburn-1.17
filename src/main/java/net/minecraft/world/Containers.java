package net.minecraft.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Containers
{
    private static final Random RANDOM = new Random();

    public static void dropContents(Level pLevel, BlockPos pX, Container p_19005_)
    {
        dropContents(pLevel, (double)pX.getX(), (double)pX.getY(), (double)pX.getZ(), p_19005_);
    }

    public static void dropContents(Level pLevel, Entity pX, Container p_19001_)
    {
        dropContents(pLevel, pX.getX(), pX.getY(), pX.getZ(), p_19001_);
    }

    private static void dropContents(Level pLevel, double pX, double p_18989_, double pY, Container p_18991_)
    {
        for (int i = 0; i < p_18991_.getContainerSize(); ++i)
        {
            dropItemStack(pLevel, pX, p_18989_, pY, p_18991_.getItem(i));
        }
    }

    public static void dropContents(Level pLevel, BlockPos pX, NonNullList<ItemStack> p_19013_)
    {
        p_19013_.forEach((p_19009_) ->
        {
            dropItemStack(pLevel, (double)pX.getX(), (double)pX.getY(), (double)pX.getZ(), p_19009_);
        });
    }

    public static void dropItemStack(Level pLevel, double pX, double p_18995_, double pY, ItemStack p_18997_)
    {
        double d0 = (double)EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(pX) + RANDOM.nextDouble() * d1 + d2;
        double d4 = Math.floor(p_18995_) + RANDOM.nextDouble() * d1;
        double d5 = Math.floor(pY) + RANDOM.nextDouble() * d1 + d2;

        while (!p_18997_.isEmpty())
        {
            ItemEntity itementity = new ItemEntity(pLevel, d3, d4, d5, p_18997_.split(RANDOM.nextInt(21) + 10));
            float f = 0.05F;
            itementity.setDeltaMovement(RANDOM.nextGaussian() * (double)0.05F, RANDOM.nextGaussian() * (double)0.05F + (double)0.2F, RANDOM.nextGaussian() * (double)0.05F);
            pLevel.addFreshEntity(itementity);
        }
    }
}
