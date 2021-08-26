package net.minecraft.client.color.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockColor
{
    int getColor(BlockState p_92567_, @Nullable BlockAndTintGetter p_92568_, @Nullable BlockPos p_92569_, int p_92570_);
}
