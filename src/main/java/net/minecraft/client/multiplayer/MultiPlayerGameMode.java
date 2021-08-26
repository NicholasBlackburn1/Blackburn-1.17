package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiPlayerGameMode
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final ClientPacketListener connection;
    private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
    private ItemStack destroyingItem = ItemStack.EMPTY;
    private float destroyProgress;
    private float destroyTicks;
    private int destroyDelay;
    private boolean isDestroying;
    private GameType localPlayerMode = GameType.DEFAULT_MODE;
    @Nullable
    private GameType previousLocalPlayerMode;
    private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, ServerboundPlayerActionPacket.Action>, Vec3> unAckedActions = new Object2ObjectLinkedOpenHashMap<>();
    private static final int MAX_ACTIONS_SIZE = 50;
    private int carriedIndex;

    public MultiPlayerGameMode(Minecraft p_105203_, ClientPacketListener p_105204_)
    {
        this.minecraft = p_105203_;
        this.connection = p_105204_;
    }

    public void adjustPlayer(Player pPlayer)
    {
        this.localPlayerMode.updatePlayerAbilities(pPlayer.getAbilities());
    }

    public void setLocalMode(GameType pType, @Nullable GameType p_171807_)
    {
        this.localPlayerMode = pType;
        this.previousLocalPlayerMode = p_171807_;
        this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
    }

    public void setLocalMode(GameType pType)
    {
        if (pType != this.localPlayerMode)
        {
            this.previousLocalPlayerMode = this.localPlayerMode;
        }

        this.localPlayerMode = pType;
        this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
    }

    public boolean canHurtPlayer()
    {
        return this.localPlayerMode.isSurvival();
    }

    public boolean destroyBlock(BlockPos pPos)
    {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, pPos, this.localPlayerMode))
        {
            return false;
        }
        else
        {
            Level level = this.minecraft.level;
            BlockState blockstate = level.getBlockState(pPos);

            if (!this.minecraft.player.getMainHandItem().getItem().canAttackBlock(blockstate, level, pPos, this.minecraft.player))
            {
                return false;
            }
            else
            {
                Block block = blockstate.getBlock();

                if (block instanceof GameMasterBlock && !this.minecraft.player.canUseGameMasterBlocks())
                {
                    return false;
                }
                else if (blockstate.isAir())
                {
                    return false;
                }
                else
                {
                    block.playerWillDestroy(level, pPos, blockstate, this.minecraft.player);
                    FluidState fluidstate = level.getFluidState(pPos);
                    boolean flag = level.setBlock(pPos, fluidstate.createLegacyBlock(), 11);

                    if (flag)
                    {
                        block.destroy(level, pPos, blockstate);
                    }

                    return flag;
                }
            }
        }
    }

    public boolean startDestroyBlock(BlockPos pLoc, Direction pFace)
    {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, pLoc, this.localPlayerMode))
        {
            return false;
        }
        else if (!this.minecraft.level.getWorldBorder().isWithinBounds(pLoc))
        {
            return false;
        }
        else
        {
            if (this.localPlayerMode.isCreative())
            {
                BlockState blockstate = this.minecraft.level.getBlockState(pLoc);
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pLoc, blockstate, 1.0F);
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pLoc, pFace);
                this.destroyBlock(pLoc);
                this.destroyDelay = 5;
            }
            else if (!this.isDestroying || !this.sameDestroyTarget(pLoc))
            {
                if (this.isDestroying)
                {
                    this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, pFace);
                }

                BlockState blockstate1 = this.minecraft.level.getBlockState(pLoc);
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pLoc, blockstate1, 0.0F);
                this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pLoc, pFace);
                boolean flag = !blockstate1.isAir();

                if (flag && this.destroyProgress == 0.0F)
                {
                    blockstate1.attack(this.minecraft.level, pLoc, this.minecraft.player);
                }

                if (flag && blockstate1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pLoc) >= 1.0F)
                {
                    this.destroyBlock(pLoc);
                }
                else
                {
                    this.isDestroying = true;
                    this.destroyBlockPos = pLoc;
                    this.destroyingItem = this.minecraft.player.getMainHandItem();
                    this.destroyProgress = 0.0F;
                    this.destroyTicks = 0.0F;
                    this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    public void stopDestroyBlock()
    {
        if (this.isDestroying)
        {
            BlockState blockstate = this.minecraft.level.getBlockState(this.destroyBlockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, blockstate, -1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN);
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, -1);
            this.minecraft.player.resetAttackStrengthTicker();
        }
    }

    public boolean continueDestroyBlock(BlockPos pPosBlock, Direction pDirectionFacing)
    {
        this.ensureHasSentCarriedItem();

        if (this.destroyDelay > 0)
        {
            --this.destroyDelay;
            return true;
        }
        else if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds(pPosBlock))
        {
            this.destroyDelay = 5;
            BlockState blockstate1 = this.minecraft.level.getBlockState(pPosBlock);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pPosBlock, blockstate1, 1.0F);
            this.sendBlockAction(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pPosBlock, pDirectionFacing);
            this.destroyBlock(pPosBlock);
            return true;
        }
        else if (this.sameDestroyTarget(pPosBlock))
        {
            BlockState blockstate = this.minecraft.level.getBlockState(pPosBlock);

            if (blockstate.isAir())
            {
                this.isDestroying = false;
                return false;
            }
            else
            {
                this.destroyProgress += blockstate.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pPosBlock);

                if (this.destroyTicks % 4.0F == 0.0F)
                {
                    SoundType soundtype = blockstate.getSoundType();
                    this.minecraft.getSoundManager().play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, pPosBlock));
                }

                ++this.destroyTicks;
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pPosBlock, blockstate, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));

                if (this.destroyProgress >= 1.0F)
                {
                    this.isDestroying = false;
                    this.sendBlockAction(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pPosBlock, pDirectionFacing);
                    this.destroyBlock(pPosBlock);
                    this.destroyProgress = 0.0F;
                    this.destroyTicks = 0.0F;
                    this.destroyDelay = 5;
                }

                this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return this.startDestroyBlock(pPosBlock, pDirectionFacing);
        }
    }

    public float getPickRange()
    {
        return this.localPlayerMode.isCreative() ? 5.0F : 4.5F;
    }

    public void tick()
    {
        this.ensureHasSentCarriedItem();

        if (this.connection.getConnection().isConnected())
        {
            this.connection.getConnection().tick();
        }
        else
        {
            this.connection.getConnection().handleDisconnection();
        }
    }

    private boolean sameDestroyTarget(BlockPos pPos)
    {
        ItemStack itemstack = this.minecraft.player.getMainHandItem();
        boolean flag = this.destroyingItem.isEmpty() && itemstack.isEmpty();

        if (!this.destroyingItem.isEmpty() && !itemstack.isEmpty())
        {
            flag = itemstack.is(this.destroyingItem.getItem()) && ItemStack.tagMatches(itemstack, this.destroyingItem) && (itemstack.isDamageableItem() || itemstack.getDamageValue() == this.destroyingItem.getDamageValue());
        }

        return pPos.equals(this.destroyBlockPos) && flag;
    }

    private void ensureHasSentCarriedItem()
    {
        int i = this.minecraft.player.getInventory().selected;

        if (i != this.carriedIndex)
        {
            this.carriedIndex = i;
            this.connection.send(new ServerboundSetCarriedItemPacket(this.carriedIndex));
        }
    }

    public InteractionResult useItemOn(LocalPlayer p_105263_, ClientLevel p_105264_, InteractionHand p_105265_, BlockHitResult p_105266_)
    {
        this.ensureHasSentCarriedItem();
        BlockPos blockpos = p_105266_.getBlockPos();

        if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockpos))
        {
            return InteractionResult.FAIL;
        }
        else
        {
            ItemStack itemstack = p_105263_.getItemInHand(p_105265_);

            if (this.localPlayerMode == GameType.SPECTATOR)
            {
                this.connection.send(new ServerboundUseItemOnPacket(p_105265_, p_105266_));
                return InteractionResult.SUCCESS;
            }
            else
            {
                boolean flag = !p_105263_.getMainHandItem().isEmpty() || !p_105263_.getOffhandItem().isEmpty();
                boolean flag1 = p_105263_.isSecondaryUseActive() && flag;

                if (!flag1)
                {
                    InteractionResult interactionresult = p_105264_.getBlockState(blockpos).use(p_105264_, p_105263_, p_105265_, p_105266_);

                    if (interactionresult.consumesAction())
                    {
                        this.connection.send(new ServerboundUseItemOnPacket(p_105265_, p_105266_));
                        return interactionresult;
                    }
                }

                this.connection.send(new ServerboundUseItemOnPacket(p_105265_, p_105266_));

                if (!itemstack.isEmpty() && !p_105263_.getCooldowns().isOnCooldown(itemstack.getItem()))
                {
                    UseOnContext useoncontext = new UseOnContext(p_105263_, p_105265_, p_105266_);
                    InteractionResult interactionresult1;

                    if (this.localPlayerMode.isCreative())
                    {
                        int i = itemstack.getCount();
                        interactionresult1 = itemstack.useOn(useoncontext);
                        itemstack.setCount(i);
                    }
                    else
                    {
                        interactionresult1 = itemstack.useOn(useoncontext);
                    }

                    return interactionresult1;
                }
                else
                {
                    return InteractionResult.PASS;
                }
            }
        }
    }

    public InteractionResult useItem(Player pPlayer, Level pLevel, InteractionHand pHand)
    {
        if (this.localPlayerMode == GameType.SPECTATOR)
        {
            return InteractionResult.PASS;
        }
        else
        {
            this.ensureHasSentCarriedItem();
            this.connection.send(new ServerboundMovePlayerPacket.PosRot(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot(), pPlayer.isOnGround()));
            this.connection.send(new ServerboundUseItemPacket(pHand));
            ItemStack itemstack = pPlayer.getItemInHand(pHand);

            if (pPlayer.getCooldowns().isOnCooldown(itemstack.getItem()))
            {
                return InteractionResult.PASS;
            }
            else
            {
                InteractionResultHolder<ItemStack> interactionresultholder = itemstack.use(pLevel, pPlayer, pHand);
                ItemStack itemstack1 = interactionresultholder.getObject();

                if (itemstack1 != itemstack)
                {
                    pPlayer.setItemInHand(pHand, itemstack1);
                }

                return interactionresultholder.getResult();
            }
        }
    }

    public LocalPlayer createPlayer(ClientLevel pLevel, StatsCounter pStatsManager, ClientRecipeBook pRecipes)
    {
        return this.createPlayer(pLevel, pStatsManager, pRecipes, false, false);
    }

    public LocalPlayer createPlayer(ClientLevel pLevel, StatsCounter pStatsManager, ClientRecipeBook pRecipes, boolean p_105254_, boolean p_105255_)
    {
        return new LocalPlayer(this.minecraft, pLevel, this.connection, pStatsManager, pRecipes, p_105254_, p_105255_);
    }

    public void attack(Player pPlayer, Entity pTargetEntity)
    {
        this.ensureHasSentCarriedItem();
        this.connection.send(ServerboundInteractPacket.createAttackPacket(pTargetEntity, pPlayer.isShiftKeyDown()));

        if (this.localPlayerMode != GameType.SPECTATOR)
        {
            pPlayer.attack(pTargetEntity);
            pPlayer.resetAttackStrengthTicker();
        }
    }

    public InteractionResult interact(Player pPlayer, Entity pTarget, InteractionHand pHand)
    {
        this.ensureHasSentCarriedItem();
        this.connection.send(ServerboundInteractPacket.createInteractionPacket(pTarget, pPlayer.isShiftKeyDown(), pHand));
        return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : pPlayer.interactOn(pTarget, pHand);
    }

    public InteractionResult interactAt(Player pPlayer, Entity pTarget, EntityHitResult pRay, InteractionHand pHand)
    {
        this.ensureHasSentCarriedItem();
        Vec3 vec3 = pRay.getLocation().subtract(pTarget.getX(), pTarget.getY(), pTarget.getZ());
        this.connection.send(ServerboundInteractPacket.createInteractionPacket(pTarget, pPlayer.isShiftKeyDown(), pHand, vec3));
        return this.localPlayerMode == GameType.SPECTATOR ? InteractionResult.PASS : pTarget.interactAt(pPlayer, vec3, pHand);
    }

    public void handleInventoryMouseClick(int p_171800_, int p_171801_, int p_171802_, ClickType p_171803_, Player p_171804_)
    {
        AbstractContainerMenu abstractcontainermenu = p_171804_.containerMenu;
        NonNullList<Slot> nonnulllist = abstractcontainermenu.slots;
        int i = nonnulllist.size();
        List<ItemStack> list = Lists.newArrayListWithCapacity(i);

        for (Slot slot : nonnulllist)
        {
            list.add(slot.getItem().copy());
        }

        abstractcontainermenu.clicked(p_171801_, p_171802_, p_171803_, p_171804_);
        Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();

        for (int j = 0; j < i; ++j)
        {
            ItemStack itemstack = list.get(j);
            ItemStack itemstack1 = nonnulllist.get(j).getItem();

            if (!ItemStack.matches(itemstack, itemstack1))
            {
                int2objectmap.put(j, itemstack1.copy());
            }
        }

        this.connection.send(new ServerboundContainerClickPacket(p_171800_, abstractcontainermenu.m_182424_(), p_171801_, p_171802_, p_171803_, abstractcontainermenu.getCarried().copy(), int2objectmap));
    }

    public void handlePlaceRecipe(int p_105218_, Recipe<?> pRecipe, boolean pPlaceAll)
    {
        this.connection.send(new ServerboundPlaceRecipePacket(p_105218_, pRecipe, pPlaceAll));
    }

    public void handleInventoryButtonClick(int pWindowID, int pButton)
    {
        this.connection.send(new ServerboundContainerButtonClickPacket(pWindowID, pButton));
    }

    public void handleCreativeModeItemAdd(ItemStack pItemStack, int pSlotId)
    {
        if (this.localPlayerMode.isCreative())
        {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(pSlotId, pItemStack));
        }
    }

    public void handleCreativeModeItemDrop(ItemStack pItemStack)
    {
        if (this.localPlayerMode.isCreative() && !pItemStack.isEmpty())
        {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(-1, pItemStack));
        }
    }

    public void releaseUsingItem(Player pPlayer)
    {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        pPlayer.releaseUsingItem();
    }

    public boolean hasExperience()
    {
        return this.localPlayerMode.isSurvival();
    }

    public boolean hasMissTime()
    {
        return !this.localPlayerMode.isCreative();
    }

    public boolean hasInfiniteItems()
    {
        return this.localPlayerMode.isCreative();
    }

    public boolean hasFarPickRange()
    {
        return this.localPlayerMode.isCreative();
    }

    public boolean isServerControlledInventory()
    {
        return this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof AbstractHorse;
    }

    public boolean isAlwaysFlying()
    {
        return this.localPlayerMode == GameType.SPECTATOR;
    }

    @Nullable
    public GameType getPreviousPlayerMode()
    {
        return this.previousLocalPlayerMode;
    }

    public GameType getPlayerMode()
    {
        return this.localPlayerMode;
    }

    public boolean isDestroying()
    {
        return this.isDestroying;
    }

    public void handlePickItem(int pIndex)
    {
        this.connection.send(new ServerboundPickItemPacket(pIndex));
    }

    private void sendBlockAction(ServerboundPlayerActionPacket.Action pAction, BlockPos pPos, Direction pDir)
    {
        LocalPlayer localplayer = this.minecraft.player;
        this.unAckedActions.put(Pair.of(pPos, pAction), localplayer.position());
        this.connection.send(new ServerboundPlayerActionPacket(pAction, pPos, pDir));
    }

    public void handleBlockBreakAck(ClientLevel pLevel, BlockPos pPos, BlockState pBlock, ServerboundPlayerActionPacket.Action pAction, boolean pSuccessful)
    {
        Vec3 vec3 = this.unAckedActions.remove(Pair.of(pPos, pAction));
        BlockState blockstate = pLevel.getBlockState(pPos);

        if ((vec3 == null || !pSuccessful || pAction != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && blockstate != pBlock) && blockstate != pBlock)
        {
            pLevel.setKnownState(pPos, pBlock);
            Player player = this.minecraft.player;

            if (vec3 != null && pLevel == player.level && player.isColliding(pPos, pBlock))
            {
                player.absMoveTo(vec3.x, vec3.y, vec3.z);
            }
        }

        while (this.unAckedActions.size() >= 50)
        {
            Pair<BlockPos, ServerboundPlayerActionPacket.Action> pair = this.unAckedActions.firstKey();
            this.unAckedActions.removeFirst();
            LOGGER.error("Too many unacked block actions, dropping {}", (Object)pair);
        }
    }
}
