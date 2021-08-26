package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements Container, MenuProvider
{
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> p_38213_, Level p_38214_)
    {
        super(p_38213_, p_38214_);
    }

    protected AbstractMinecartContainer(EntityType<?> p_38207_, double p_38208_, double p_38209_, double p_38210_, Level p_38211_)
    {
        super(p_38207_, p_38211_, p_38208_, p_38209_, p_38210_);
    }

    public void destroy(DamageSource pSource)
    {
        super.destroy(pSource);

        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            Containers.dropContents(this.level, this, this);

            if (!this.level.isClientSide)
            {
                Entity entity = pSource.getDirectEntity();

                if (entity != null && entity.getType() == EntityType.PLAYER)
                {
                    PiglinAi.angerNearbyPiglins((Player)entity, true);
                }
            }
        }
    }

    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.itemStacks)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public ItemStack getItem(int pIndex)
    {
        this.unpackLootTable((Player)null);
        return this.itemStacks.get(pIndex);
    }

    public ItemStack removeItem(int pIndex, int pCount)
    {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.itemStacks, pIndex, pCount);
    }

    public ItemStack removeItemNoUpdate(int pIndex)
    {
        this.unpackLootTable((Player)null);
        ItemStack itemstack = this.itemStacks.get(pIndex);

        if (itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            this.itemStacks.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setItem(int pIndex, ItemStack pStack)
    {
        this.unpackLootTable((Player)null);
        this.itemStacks.set(pIndex, pStack);

        if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize())
        {
            pStack.setCount(this.getMaxStackSize());
        }
    }

    public SlotAccess getSlot(final int p_150257_)
    {
        return p_150257_ >= 0 && p_150257_ < this.getContainerSize() ? new SlotAccess()
        {
            public ItemStack get()
            {
                return AbstractMinecartContainer.this.getItem(p_150257_);
            }
            public boolean set(ItemStack p_150265_)
            {
                AbstractMinecartContainer.this.setItem(p_150257_, p_150265_);
                return true;
            }
        } : super.getSlot(p_150257_);
    }

    public void setChanged()
    {
    }

    public boolean stillValid(Player pPlayer)
    {
        if (this.isRemoved())
        {
            return false;
        }
        else
        {
            return !(pPlayer.distanceToSqr(this) > 64.0D);
        }
    }

    public void remove(Entity.RemovalReason p_150255_)
    {
        if (!this.level.isClientSide && p_150255_.shouldDestroy())
        {
            Containers.dropContents(this.level, this, this);
        }

        super.remove(p_150255_);
    }

    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);

        if (this.lootTable != null)
        {
            pCompound.putString("LootTable", this.lootTable.toString());

            if (this.lootTableSeed != 0L)
            {
                pCompound.putLong("LootTableSeed", this.lootTableSeed);
            }
        }
        else
        {
            ContainerHelper.saveAllItems(pCompound, this.itemStacks);
        }
    }

    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        if (pCompound.contains("LootTable", 8))
        {
            this.lootTable = new ResourceLocation(pCompound.getString("LootTable"));
            this.lootTableSeed = pCompound.getLong("LootTableSeed");
        }
        else
        {
            ContainerHelper.loadAllItems(pCompound, this.itemStacks);
        }
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand)
    {
        pPlayer.openMenu(this);

        if (!pPlayer.level.isClientSide)
        {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
            return InteractionResult.CONSUME;
        }
        else
        {
            return InteractionResult.SUCCESS;
        }
    }

    protected void applyNaturalSlowdown()
    {
        float f = 0.98F;

        if (this.lootTable == null)
        {
            int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            f += (float)i * 0.001F;
        }

        if (this.isInWater())
        {
            f *= 0.95F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0D, (double)f));
    }

    public void unpackLootTable(@Nullable Player pPlayer)
    {
        if (this.lootTable != null && this.level.getServer() != null)
        {
            LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);

            if (pPlayer instanceof ServerPlayer)
            {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)pPlayer, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, this.position()).withOptionalRandomSeed(this.lootTableSeed);

            if (pPlayer != null)
            {
                lootcontext$builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
            }

            loottable.fill(this, lootcontext$builder.create(LootContextParamSets.CHEST));
        }
    }

    public void clearContent()
    {
        this.unpackLootTable((Player)null);
        this.itemStacks.clear();
    }

    public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed)
    {
        this.lootTable = pLootTable;
        this.lootTableSeed = pLootTableSeed;
    }

    @Nullable
    public AbstractContainerMenu createMenu(int pId, Inventory pPlayerInventory, Player p_38253_)
    {
        if (this.lootTable != null && p_38253_.isSpectator())
        {
            return null;
        }
        else
        {
            this.unpackLootTable(pPlayerInventory.player);
            return this.createMenu(pId, pPlayerInventory);
        }
    }

    protected abstract AbstractContainerMenu createMenu(int pId, Inventory pPlayerInventory);
}
