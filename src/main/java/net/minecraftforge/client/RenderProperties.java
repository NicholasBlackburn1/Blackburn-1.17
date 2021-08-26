package net.minecraftforge.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RenderProperties
{
    public static IBlockRenderProperties get(BlockState state)
    {
        return IBlockRenderProperties.DUMMY;
    }

    public static IItemRenderProperties get(ItemStack itemStack)
    {
        return IItemRenderProperties.DUMMY;
    }
}
