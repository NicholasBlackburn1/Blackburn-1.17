package net.minecraft.client.color.item;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemColors
{
    private static final int DEFAULT = -1;
    private final IdMapper<ItemColor> itemColors = new IdMapper<>(32);

    public static ItemColors createDefault(BlockColors pColors)
    {
        ItemColors itemcolors = new ItemColors();
        itemcolors.m_92689_((p_92708_, p_92709_) ->
        {
            return p_92709_ > 0 ? -1 : ((DyeableLeatherItem)p_92708_.getItem()).getColor(p_92708_);
        }, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
        itemcolors.m_92689_((p_92705_, p_92706_) ->
        {
            return GrassColor.get(0.5D, 1.0D);
        }, Blocks.TALL_GRASS, Blocks.LARGE_FERN);
        itemcolors.m_92689_((p_92702_, p_92703_) ->
        {
            if (p_92703_ != 1)
            {
                return -1;
            }
            else {
                CompoundTag compoundtag = p_92702_.getTagElement("Explosion");
                int[] aint = compoundtag != null && compoundtag.contains("Colors", 11) ? compoundtag.getIntArray("Colors") : null;

                if (aint != null && aint.length != 0)
                {
                    if (aint.length == 1)
                    {
                        return aint[0];
                    }
                    else
                    {
                        int i = 0;
                        int j = 0;
                        int k = 0;

                        for (int l : aint)
                        {
                            i += (l & 16711680) >> 16;
                            j += (l & 65280) >> 8;
                            k += (l & 255) >> 0;
                        }

                        i = i / aint.length;
                        j = j / aint.length;
                        k = k / aint.length;
                        return i << 16 | j << 8 | k;
                    }
                }
                else {
                    return 9079434;
                }
            }
        }, Items.FIREWORK_STAR);
        itemcolors.m_92689_((p_92699_, p_92700_) ->
        {
            return p_92700_ > 0 ? -1 : PotionUtils.getColor(p_92699_);
        }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

        for (SpawnEggItem spawneggitem : SpawnEggItem.eggs())
        {
            itemcolors.m_92689_((p_92681_, p_92682_) ->
            {
                return spawneggitem.getColor(p_92682_);
            }, spawneggitem);
        }

        itemcolors.m_92689_((p_92687_, p_92688_) ->
        {
            BlockState blockstate = ((BlockItem)p_92687_.getItem()).getBlock().defaultBlockState();
            return pColors.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, p_92688_);
        }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
        itemcolors.m_92689_((p_92696_, p_92697_) ->
        {
            return p_92697_ == 0 ? PotionUtils.getColor(p_92696_) : -1;
        }, Items.TIPPED_ARROW);
        itemcolors.m_92689_((p_92693_, p_92694_) ->
        {
            return p_92694_ == 0 ? -1 : MapItem.getColor(p_92693_);
        }, Items.FILLED_MAP);
        return itemcolors;
    }

    public int getColor(ItemStack pStack, int pTintIndex)
    {
        ItemColor itemcolor = this.itemColors.byId(Registry.ITEM.getId(pStack.getItem()));
        return itemcolor == null ? -1 : itemcolor.getColor(pStack, pTintIndex);
    }

    public void m_92689_(ItemColor p_92690_, ItemLike... p_92691_)
    {
        for (ItemLike itemlike : p_92691_)
        {
            this.itemColors.addMapping(p_92690_, Item.getId(itemlike.asItem()));
        }
    }
}
