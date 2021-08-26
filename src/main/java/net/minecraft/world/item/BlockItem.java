package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockItem extends Item
{
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String BLOCK_STATE_TAG = "BlockStateTag";
    @Deprecated
    private final Block block;

    public BlockItem(Block p_40565_, Item.Properties p_40566_)
    {
        super(p_40566_);
        this.block = p_40565_;
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        InteractionResult interactionresult = this.place(new BlockPlaceContext(pContext));

        if (!interactionresult.consumesAction() && this.isEdible())
        {
            InteractionResult interactionresult1 = this.use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult();
            return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
        }
        else
        {
            return interactionresult;
        }
    }

    public InteractionResult place(BlockPlaceContext pContext)
    {
        if (!pContext.canPlace())
        {
            return InteractionResult.FAIL;
        }
        else
        {
            BlockPlaceContext blockplacecontext = this.updatePlacementContext(pContext);

            if (blockplacecontext == null)
            {
                return InteractionResult.FAIL;
            }
            else
            {
                BlockState blockstate = this.getPlacementState(blockplacecontext);

                if (blockstate == null)
                {
                    return InteractionResult.FAIL;
                }
                else if (!this.placeBlock(blockplacecontext, blockstate))
                {
                    return InteractionResult.FAIL;
                }
                else
                {
                    BlockPos blockpos = blockplacecontext.getClickedPos();
                    Level level = blockplacecontext.getLevel();
                    Player player = blockplacecontext.getPlayer();
                    ItemStack itemstack = blockplacecontext.getItemInHand();
                    BlockState blockstate1 = level.getBlockState(blockpos);

                    if (blockstate1.is(blockstate.getBlock()))
                    {
                        blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
                        this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
                        blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);

                        if (player instanceof ServerPlayer)
                        {
                            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
                        }
                    }

                    SoundType soundtype = blockstate1.getSoundType();
                    level.playSound(player, blockpos, this.getPlaceSound(blockstate1), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);

                    if (player == null || !player.getAbilities().instabuild)
                    {
                        itemstack.shrink(1);
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
    }

    protected SoundEvent getPlaceSound(BlockState pState)
    {
        return pState.getSoundType().getPlaceSound();
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext pContext)
    {
        return pContext;
    }

    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState)
    {
        return updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext pContext)
    {
        BlockState blockstate = this.getBlock().getStateForPlacement(pContext);
        return blockstate != null && this.canPlace(pContext, blockstate) ? blockstate : null;
    }

    private BlockState updateBlockStateFromTag(BlockPos p_40603_, Level p_40604_, ItemStack p_40605_, BlockState p_40606_)
    {
        BlockState blockstate = p_40606_;
        CompoundTag compoundtag = p_40605_.getTag();

        if (compoundtag != null)
        {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statedefinition = p_40606_.getBlock().getStateDefinition();

            for (String s : compoundtag1.getAllKeys())
            {
                Property<?> property = statedefinition.getProperty(s);

                if (property != null)
                {
                    String s1 = compoundtag1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != p_40606_)
        {
            p_40604_.setBlock(p_40603_, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState p_40594_, Property<T> p_40595_, String p_40596_)
    {
        return p_40595_.getValue(p_40596_).map((p_40592_) ->
        {
            return p_40594_.setValue(p_40595_, p_40592_);
        }).orElse(p_40594_);
    }

    protected boolean canPlace(BlockPlaceContext p_40611_, BlockState p_40612_)
    {
        Player player = p_40611_.getPlayer();
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (!this.mustSurvive() || p_40612_.canSurvive(p_40611_.getLevel(), p_40611_.getClickedPos())) && p_40611_.getLevel().isUnobstructed(p_40612_, p_40611_.getClickedPos(), collisioncontext);
    }

    protected boolean mustSurvive()
    {
        return true;
    }

    protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState)
    {
        return pContext.getLevel().setBlock(pContext.getClickedPos(), pState, 11);
    }

    public static boolean updateCustomBlockEntityTag(Level p_40583_, @Nullable Player pPos, BlockPos pLevel, ItemStack pPlayer)
    {
        MinecraftServer minecraftserver = p_40583_.getServer();

        if (minecraftserver == null)
        {
            return false;
        }
        else
        {
            CompoundTag compoundtag = pPlayer.getTagElement("BlockEntityTag");

            if (compoundtag != null)
            {
                BlockEntity blockentity = p_40583_.getBlockEntity(pLevel);

                if (blockentity != null)
                {
                    if (!p_40583_.isClientSide && blockentity.onlyOpCanSetNbt() && (pPos == null || !pPos.canUseGameMasterBlocks()))
                    {
                        return false;
                    }

                    CompoundTag compoundtag1 = blockentity.save(new CompoundTag());
                    CompoundTag compoundtag2 = compoundtag1.copy();
                    compoundtag1.merge(compoundtag);
                    compoundtag1.putInt("x", pLevel.getX());
                    compoundtag1.putInt("y", pLevel.getY());
                    compoundtag1.putInt("z", pLevel.getZ());

                    if (!compoundtag1.equals(compoundtag2))
                    {
                        blockentity.load(compoundtag1);
                        blockentity.setChanged();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public String getDescriptionId()
    {
        return this.getBlock().getDescriptionId();
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        if (this.allowdedIn(pGroup))
        {
            this.getBlock().fillItemCategory(pGroup, pItems);
        }
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        this.getBlock().appendHoverText(pStack, pLevel, pTooltip, pFlag);
    }

    public Block getBlock()
    {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem)
    {
        pBlockToItemMap.put(this.getBlock(), pItem);
    }

    public boolean canFitInsideContainerItems()
    {
        return !(this.block instanceof ShulkerBoxBlock);
    }

    public void onDestroyed(ItemEntity p_150700_)
    {
        if (this.block instanceof ShulkerBoxBlock)
        {
            CompoundTag compoundtag = p_150700_.getItem().getTag();

            if (compoundtag != null)
            {
                ListTag listtag = compoundtag.getCompound("BlockEntityTag").getList("Items", 10);
                ItemUtils.onContainerDestroyed(p_150700_, listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of));
            }
        }
    }
}
