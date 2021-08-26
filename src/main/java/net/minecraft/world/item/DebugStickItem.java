package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends Item
{
    public DebugStickItem(Item.Properties p_40948_)
    {
        super(p_40948_);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return true;
    }

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {
        if (!pLevel.isClientSide)
        {
            this.handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(InteractionHand.MAIN_HAND));
        }

        return false;
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();

        if (!level.isClientSide && player != null)
        {
            BlockPos blockpos = pContext.getClickedPos();

            if (!this.handleInteraction(player, level.getBlockState(blockpos), level, blockpos, true, pContext.getItemInHand()))
            {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean handleInteraction(Player p_150803_, BlockState p_150804_, LevelAccessor p_150805_, BlockPos p_150806_, boolean p_150807_, ItemStack p_150808_)
    {
        if (!p_150803_.canUseGameMasterBlocks())
        {
            return false;
        }
        else
        {
            Block block = p_150804_.getBlock();
            StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
            Collection < Property<? >> collection = statedefinition.getProperties();
            String s = Registry.BLOCK.getKey(block).toString();

            if (collection.isEmpty())
            {
                message(p_150803_, new TranslatableComponent(this.getDescriptionId() + ".empty", s));
                return false;
            }
            else
            {
                CompoundTag compoundtag = p_150808_.getOrCreateTagElement("DebugProperty");
                String s1 = compoundtag.getString(s);
                Property<?> property = statedefinition.getProperty(s1);

                if (p_150807_)
                {
                    if (property == null)
                    {
                        property = collection.iterator().next();
                    }

                    BlockState blockstate = cycleState(p_150804_, property, p_150803_.isSecondaryUseActive());
                    p_150805_.setBlock(p_150806_, blockstate, 18);
                    message(p_150803_, new TranslatableComponent(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate, property)));
                }
                else
                {
                    property = getRelative(collection, property, p_150803_.isSecondaryUseActive());
                    String s2 = property.getName();
                    compoundtag.putString(s, s2);
                    message(p_150803_, new TranslatableComponent(this.getDescriptionId() + ".select", s2, getNameHelper(p_150804_, property)));
                }

                return true;
            }
        }
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards)
    {
        return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
    }

    private static <T> T getRelative(Iterable<T> pAllowedValues, @Nullable T pCurrentValue, boolean pBackwards)
    {
        return (T)(pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue));
    }

    private static void message(Player pPlayer, Component pText)
    {
        ((ServerPlayer)pPlayer).sendMessage(pText, ChatType.GAME_INFO, Util.NIL_UUID);
    }

    private static <T extends Comparable<T>> String getNameHelper(BlockState p_40967_, Property<T> p_40968_)
    {
        return p_40968_.getName(p_40967_.getValue(p_40968_));
    }
}
