package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FrostWalkerEnchantment extends Enchantment
{
    public FrostWalkerEnchantment(Enchantment.Rarity p_45013_, EquipmentSlot... p_45014_)
    {
        super(p_45013_, EnchantmentCategory.ARMOR_FEET, p_45014_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return pEnchantmentLevel * 10;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 15;
    }

    public boolean isTreasureOnly()
    {
        return true;
    }

    public int getMaxLevel()
    {
        return 2;
    }

    public static void onEntityMoved(LivingEntity pLiving, Level pLevel, BlockPos pPos, int pLevelConflicting)
    {
        if (pLiving.isOnGround())
        {
            BlockState blockstate = Blocks.FROSTED_ICE.defaultBlockState();
            float f = (float)Math.min(16, 2 + pLevelConflicting);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset((double)(-f), -1.0D, (double)(-f)), pPos.offset((double)f, -1.0D, (double)f)))
            {
                if (blockpos.closerThan(pLiving.position(), (double)f))
                {
                    blockpos$mutableblockpos.set(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
                    BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);

                    if (blockstate1.isAir())
                    {
                        BlockState blockstate2 = pLevel.getBlockState(blockpos);

                        if (blockstate2.getMaterial() == Material.WATER && blockstate2.getValue(LiquidBlock.LEVEL) == 0 && blockstate.canSurvive(pLevel, blockpos) && pLevel.isUnobstructed(blockstate, blockpos, CollisionContext.empty()))
                        {
                            pLevel.setBlockAndUpdate(blockpos, blockstate);
                            pLevel.getBlockTicks().scheduleTick(blockpos, Blocks.FROSTED_ICE, Mth.nextInt(pLiving.getRandom(), 60, 120));
                        }
                    }
                }
            }
        }
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.DEPTH_STRIDER;
    }
}
