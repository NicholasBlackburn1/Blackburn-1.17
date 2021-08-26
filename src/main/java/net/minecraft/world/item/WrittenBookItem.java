package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WrittenBookItem extends Item
{
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int PAGE_EDIT_LENGTH = 1024;
    public static final int PAGE_LENGTH = 32767;
    public static final int MAX_PAGES = 100;
    public static final int MAX_GENERATION = 2;
    public static final String TAG_TITLE = "title";
    public static final String TAG_FILTERED_TITLE = "filtered_title";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_PAGES = "pages";
    public static final String TAG_FILTERED_PAGES = "filtered_pages";
    public static final String TAG_GENERATION = "generation";
    public static final String TAG_RESOLVED = "resolved";

    public WrittenBookItem(Item.Properties p_43455_)
    {
        super(p_43455_);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag pNbt)
    {
        if (!WritableBookItem.makeSureTagIsValid(pNbt))
        {
            return false;
        }
        else if (!pNbt.contains("title", 8))
        {
            return false;
        }
        else
        {
            String s = pNbt.getString("title");
            return s.length() > 32 ? false : pNbt.contains("author", 8);
        }
    }

    public static int getGeneration(ItemStack pBook)
    {
        return pBook.getTag().getInt("generation");
    }

    public static int getPageCount(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null ? compoundtag.getList("pages", 8).size() : 0;
    }

    public Component getName(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();

        if (compoundtag != null)
        {
            String s = compoundtag.getString("title");

            if (!StringUtil.isNullOrEmpty(s))
            {
                return new TextComponent(s);
            }
        }

        return super.getName(pStack);
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        if (pStack.hasTag())
        {
            CompoundTag compoundtag = pStack.getTag();
            String s = compoundtag.getString("author");

            if (!StringUtil.isNullOrEmpty(s))
            {
                pTooltip.add((new TranslatableComponent("book.byAuthor", s)).withStyle(ChatFormatting.GRAY));
            }

            pTooltip.add((new TranslatableComponent("book.generation." + compoundtag.getInt("generation"))).withStyle(ChatFormatting.GRAY));
        }
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);

        if (blockstate.is(Blocks.LECTERN))
        {
            return LecternBlock.tryPlaceBook(pContext.getPlayer(), level, blockpos, blockstate, pContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.openItemGui(itemstack, pHand);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    public static boolean resolveBookComponents(ItemStack pStack, @Nullable CommandSourceStack pResolvingSource, @Nullable Player pResolvingPlayer)
    {
        CompoundTag compoundtag = pStack.getTag();

        if (compoundtag != null && !compoundtag.getBoolean("resolved"))
        {
            compoundtag.putBoolean("resolved", true);

            if (!makeSureTagIsValid(compoundtag))
            {
                return false;
            }
            else
            {
                ListTag listtag = compoundtag.getList("pages", 8);

                for (int i = 0; i < listtag.size(); ++i)
                {
                    listtag.set(i, (Tag)StringTag.valueOf(resolvePage(pResolvingSource, pResolvingPlayer, listtag.getString(i))));
                }

                if (compoundtag.contains("filtered_pages", 10))
                {
                    CompoundTag compoundtag1 = compoundtag.getCompound("filtered_pages");

                    for (String s : compoundtag1.getAllKeys())
                    {
                        compoundtag1.putString(s, resolvePage(pResolvingSource, pResolvingPlayer, compoundtag1.getString(s)));
                    }
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    private static String resolvePage(@Nullable CommandSourceStack p_151249_, @Nullable Player p_151250_, String p_151251_)
    {
        Component component;

        try
        {
            component = Component.Serializer.fromJsonLenient(p_151251_);
            component = ComponentUtils.updateForEntity(p_151249_, component, p_151250_, 0);
        }
        catch (Exception exception)
        {
            component = new TextComponent(p_151251_);
        }

        return Component.Serializer.toJson(component);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return true;
    }
}
