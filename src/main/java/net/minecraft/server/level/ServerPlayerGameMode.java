package net.minecraft.server.level;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlayerGameMode
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected ServerLevel level;
    protected final ServerPlayer player;
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
    @Nullable
    private GameType previousGameModeForPlayer;
    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos = BlockPos.ZERO;
    private int delayedTickStart;
    private int lastSentState = -1;

    public ServerPlayerGameMode(ServerPlayer p_143472_)
    {
        this.player = p_143472_;
        this.level = p_143472_.getLevel();
    }

    public boolean changeGameModeForPlayer(GameType p_143474_)
    {
        if (p_143474_ == this.gameModeForPlayer)
        {
            return false;
        }
        else
        {
            this.setGameModeForPlayer(p_143474_, this.gameModeForPlayer);
            return true;
        }
    }

    protected void setGameModeForPlayer(GameType p_9274_, @Nullable GameType p_9275_)
    {
        this.previousGameModeForPlayer = p_9275_;
        this.gameModeForPlayer = p_9274_;
        p_9274_.updatePlayerAbilities(this.player.getAbilities());
        this.player.onUpdateAbilities();
        this.player.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, this.player));
        this.level.updateSleepingPlayerList();
    }

    public GameType getGameModeForPlayer()
    {
        return this.gameModeForPlayer;
    }

    @Nullable
    public GameType getPreviousGameModeForPlayer()
    {
        return this.previousGameModeForPlayer;
    }

    public boolean isSurvival()
    {
        return this.gameModeForPlayer.isSurvival();
    }

    public boolean isCreative()
    {
        return this.gameModeForPlayer.isCreative();
    }

    public void tick()
    {
        ++this.gameTicks;

        if (this.hasDelayedDestroy)
        {
            BlockState blockstate = this.level.getBlockState(this.delayedDestroyPos);

            if (blockstate.isAir())
            {
                this.hasDelayedDestroy = false;
            }
            else
            {
                float f = this.incrementDestroyProgress(blockstate, this.delayedDestroyPos, this.delayedTickStart);

                if (f >= 1.0F)
                {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }
        }
        else if (this.isDestroyingBlock)
        {
            BlockState blockstate1 = this.level.getBlockState(this.destroyPos);

            if (blockstate1.isAir())
            {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            }
            else
            {
                this.incrementDestroyProgress(blockstate1, this.destroyPos, this.destroyProgressStart);
            }
        }
    }

    private float incrementDestroyProgress(BlockState p_9277_, BlockPos p_9278_, int p_9279_)
    {
        int i = this.gameTicks - p_9279_;
        float f = p_9277_.getDestroyProgress(this.player, this.player.level, p_9278_) * (float)(i + 1);
        int j = (int)(f * 10.0F);

        if (j != this.lastSentState)
        {
            this.level.destroyBlockProgress(this.player.getId(), p_9278_, j);
            this.lastSentState = j;
        }

        return f;
    }

    public void handleBlockBreakAction(BlockPos p_9282_, ServerboundPlayerActionPacket.Action p_9283_, Direction p_9284_, int p_9285_)
    {
        double d0 = this.player.getX() - ((double)p_9282_.getX() + 0.5D);
        double d1 = this.player.getY() - ((double)p_9282_.getY() + 0.5D) + 1.5D;
        double d2 = this.player.getZ() - ((double)p_9282_.getZ() + 0.5D);
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

        if (d3 > 36.0D)
        {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, false, "too far"));
        }
        else if (p_9282_.getY() >= p_9285_)
        {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, false, "too high"));
        }
        else
        {
            if (p_9283_ == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK)
            {
                if (!this.level.mayInteract(this.player, p_9282_))
                {
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, false, "may not interact"));
                    return;
                }

                if (this.isCreative())
                {
                    this.destroyAndAck(p_9282_, p_9283_, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, p_9282_, this.gameModeForPlayer))
                {
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, false, "block action restricted"));
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float f = 1.0F;
                BlockState blockstate = this.level.getBlockState(p_9282_);

                if (!blockstate.isAir())
                {
                    blockstate.attack(this.level, p_9282_, this.player);
                    f = blockstate.getDestroyProgress(this.player, this.player.level, p_9282_);
                }

                if (!blockstate.isAir() && f >= 1.0F)
                {
                    this.destroyAndAck(p_9282_, p_9283_, "insta mine");
                }
                else
                {
                    if (this.isDestroyingBlock)
                    {
                        this.player.connection.send(new ClientboundBlockBreakAckPacket(this.destroyPos, this.level.getBlockState(this.destroyPos), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, false, "abort destroying since another started (client insta mine, server disagreed)"));
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = p_9282_.immutable();
                    int i = (int)(f * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), p_9282_, i);
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, true, "actual start of destroying"));
                    this.lastSentState = i;
                }
            }
            else if (p_9283_ == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK)
            {
                if (p_9282_.equals(this.destroyPos))
                {
                    int j = this.gameTicks - this.destroyProgressStart;
                    BlockState blockstate1 = this.level.getBlockState(p_9282_);

                    if (!blockstate1.isAir())
                    {
                        float f1 = blockstate1.getDestroyProgress(this.player, this.player.level, p_9282_) * (float)(j + 1);

                        if (f1 >= 0.7F)
                        {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), p_9282_, -1);
                            this.destroyAndAck(p_9282_, p_9283_, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy)
                        {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = p_9282_;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, true, "stopped destroying"));
            }
            else if (p_9283_ == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK)
            {
                this.isDestroyingBlock = false;

                if (!Objects.equals(this.destroyPos, p_9282_))
                {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, p_9282_);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(this.destroyPos, this.level.getBlockState(this.destroyPos), p_9283_, true, "aborted mismatched destroying"));
                }

                this.level.destroyBlockProgress(this.player.getId(), p_9282_, -1);
                this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9282_, this.level.getBlockState(p_9282_), p_9283_, true, "aborted destroying"));
            }
        }
    }

    public void destroyAndAck(BlockPos p_9287_, ServerboundPlayerActionPacket.Action p_9288_, String p_9289_)
    {
        if (this.destroyBlock(p_9287_))
        {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9287_, this.level.getBlockState(p_9287_), p_9288_, true, p_9289_));
        }
        else
        {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(p_9287_, this.level.getBlockState(p_9287_), p_9288_, false, p_9289_));
        }
    }

    public boolean destroyBlock(BlockPos pPos)
    {
        BlockState blockstate = this.level.getBlockState(pPos);

        if (!this.player.getMainHandItem().getItem().canAttackBlock(blockstate, this.level, pPos, this.player))
        {
            return false;
        }
        else
        {
            BlockEntity blockentity = this.level.getBlockEntity(pPos);
            Block block = blockstate.getBlock();

            if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks())
            {
                this.level.sendBlockUpdated(pPos, blockstate, blockstate, 3);
                return false;
            }
            else if (this.player.blockActionRestricted(this.level, pPos, this.gameModeForPlayer))
            {
                return false;
            }
            else
            {
                block.playerWillDestroy(this.level, pPos, blockstate, this.player);
                boolean flag = this.level.removeBlock(pPos, false);

                if (flag)
                {
                    block.destroy(this.level, pPos, blockstate);
                }

                if (this.isCreative())
                {
                    return true;
                }
                else
                {
                    ItemStack itemstack = this.player.getMainHandItem();
                    ItemStack itemstack1 = itemstack.copy();
                    boolean flag1 = this.player.hasCorrectToolForDrops(blockstate);
                    itemstack.mineBlock(this.level, blockstate, pPos, this.player);

                    if (flag && flag1)
                    {
                        block.playerDestroy(this.level, this.player, pPos, blockstate, blockentity, itemstack1);
                    }

                    return true;
                }
            }
        }
    }

    public InteractionResult useItem(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand)
    {
        if (this.gameModeForPlayer == GameType.SPECTATOR)
        {
            return InteractionResult.PASS;
        }
        else if (pPlayer.getCooldowns().isOnCooldown(pStack.getItem()))
        {
            return InteractionResult.PASS;
        }
        else
        {
            int i = pStack.getCount();
            int j = pStack.getDamageValue();
            InteractionResultHolder<ItemStack> interactionresultholder = pStack.use(pLevel, pPlayer, pHand);
            ItemStack itemstack = interactionresultholder.getObject();

            if (itemstack == pStack && itemstack.getCount() == i && itemstack.getUseDuration() <= 0 && itemstack.getDamageValue() == j)
            {
                return interactionresultholder.getResult();
            }
            else if (interactionresultholder.getResult() == InteractionResult.FAIL && itemstack.getUseDuration() > 0 && !pPlayer.isUsingItem())
            {
                return interactionresultholder.getResult();
            }
            else
            {
                pPlayer.setItemInHand(pHand, itemstack);

                if (this.isCreative())
                {
                    itemstack.setCount(i);

                    if (itemstack.isDamageableItem() && itemstack.getDamageValue() != j)
                    {
                        itemstack.setDamageValue(j);
                    }
                }

                if (itemstack.isEmpty())
                {
                    pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
                }

                if (!pPlayer.isUsingItem())
                {
                    pPlayer.inventoryMenu.sendAllDataToRemote();
                }

                return interactionresultholder.getResult();
            }
        }
    }

    public InteractionResult useItemOn(ServerPlayer p_9266_, Level p_9267_, ItemStack p_9268_, InteractionHand p_9269_, BlockHitResult p_9270_)
    {
        BlockPos blockpos = p_9270_.getBlockPos();
        BlockState blockstate = p_9267_.getBlockState(blockpos);

        if (this.gameModeForPlayer == GameType.SPECTATOR)
        {
            MenuProvider menuprovider = blockstate.getMenuProvider(p_9267_, blockpos);

            if (menuprovider != null)
            {
                p_9266_.openMenu(menuprovider);
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.PASS;
            }
        }
        else
        {
            boolean flag = !p_9266_.getMainHandItem().isEmpty() || !p_9266_.getOffhandItem().isEmpty();
            boolean flag1 = p_9266_.isSecondaryUseActive() && flag;
            ItemStack itemstack = p_9268_.copy();

            if (!flag1)
            {
                InteractionResult interactionresult = blockstate.use(p_9267_, p_9266_, p_9269_, p_9270_);

                if (interactionresult.consumesAction())
                {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(p_9266_, blockpos, itemstack);
                    return interactionresult;
                }
            }

            if (!p_9268_.isEmpty() && !p_9266_.getCooldowns().isOnCooldown(p_9268_.getItem()))
            {
                UseOnContext useoncontext = new UseOnContext(p_9266_, p_9269_, p_9270_);
                InteractionResult interactionresult1;

                if (this.isCreative())
                {
                    int i = p_9268_.getCount();
                    interactionresult1 = p_9268_.useOn(useoncontext);
                    p_9268_.setCount(i);
                }
                else
                {
                    interactionresult1 = p_9268_.useOn(useoncontext);
                }

                if (interactionresult1.consumesAction())
                {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(p_9266_, blockpos, itemstack);
                }

                return interactionresult1;
            }
            else
            {
                return InteractionResult.PASS;
            }
        }
    }

    public void setLevel(ServerLevel pServerLevel)
    {
        this.level = pServerLevel;
    }
}
