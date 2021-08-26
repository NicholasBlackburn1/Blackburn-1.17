package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem
{
    private static final String PATTERN_PREFIX = "block.minecraft.banner.";

    public BannerItem(Block p_40534_, Block p_40535_, Item.Properties p_40536_)
    {
        super(p_40534_, p_40535_, p_40536_);
        Validate.isInstanceOf(AbstractBannerBlock.class, p_40534_);
        Validate.isInstanceOf(AbstractBannerBlock.class, p_40535_);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack pStack, List<Component> pTooltip)
    {
        CompoundTag compoundtag = pStack.getTagElement("BlockEntityTag");

        if (compoundtag != null && compoundtag.contains("Patterns"))
        {
            ListTag listtag = compoundtag.getList("Patterns", 10);

            for (int i = 0; i < listtag.size() && i < 6; ++i)
            {
                CompoundTag compoundtag1 = listtag.getCompound(i);
                DyeColor dyecolor = DyeColor.byId(compoundtag1.getInt("Color"));
                BannerPattern bannerpattern = BannerPattern.byHash(compoundtag1.getString("Pattern"));

                if (bannerpattern != null)
                {
                    pTooltip.add((new TranslatableComponent("block.minecraft.banner." + bannerpattern.getFilename() + "." + dyecolor.getName())).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    public DyeColor getColor()
    {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        appendHoverTextFromBannerBlockEntityTag(pStack, pTooltip);
    }
}
