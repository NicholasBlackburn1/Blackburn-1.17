package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class JukeboxBlockEntity extends BlockEntity implements Clearable
{
    private ItemStack record = ItemStack.EMPTY;

    public JukeboxBlockEntity(BlockPos p_155613_, BlockState p_155614_)
    {
        super(BlockEntityType.JUKEBOX, p_155613_, p_155614_);
    }

    public void load(CompoundTag p_155616_)
    {
        super.load(p_155616_);

        if (p_155616_.contains("RecordItem", 10))
        {
            this.setRecord(ItemStack.of(p_155616_.getCompound("RecordItem")));
        }
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);

        if (!this.getRecord().isEmpty())
        {
            pCompound.put("RecordItem", this.getRecord().save(new CompoundTag()));
        }

        return pCompound;
    }

    public ItemStack getRecord()
    {
        return this.record;
    }

    public void setRecord(ItemStack p_59518_)
    {
        this.record = p_59518_;
        this.setChanged();
    }

    public void clearContent()
    {
        this.setRecord(ItemStack.EMPTY);
    }
}
