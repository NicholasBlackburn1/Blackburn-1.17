package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity
{
    private int output;

    public ComparatorBlockEntity(BlockPos p_155386_, BlockState p_155387_)
    {
        super(BlockEntityType.COMPARATOR, p_155386_, p_155387_);
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);
        pCompound.putInt("OutputSignal", this.output);
        return pCompound;
    }

    public void load(CompoundTag p_155389_)
    {
        super.load(p_155389_);
        this.output = p_155389_.getInt("OutputSignal");
    }

    public int getOutputSignal()
    {
        return this.output;
    }

    public void setOutputSignal(int pOutputSignal)
    {
        this.output = pOutputSignal;
    }
}
