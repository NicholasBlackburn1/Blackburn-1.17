package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item
{
    private final Block block;

    public AirItem(Block p_40368_, Item.Properties p_40369_)
    {
        super(p_40369_);
        this.block = p_40368_;
    }

    public String getDescriptionId()
    {
        return this.block.getDescriptionId();
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        this.block.appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }
}
