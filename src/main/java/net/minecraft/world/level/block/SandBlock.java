package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock
{
    private final int dustColor;

    public SandBlock(int p_55967_, BlockBehaviour.Properties p_55968_)
    {
        super(p_55968_);
        this.dustColor = p_55967_;
    }

    public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        return this.dustColor;
    }
}
