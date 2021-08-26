package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper
{
    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public HopperBlockEntity(BlockPos p_155550_, BlockState p_155551_)
    {
        super(BlockEntityType.HOPPER, p_155550_, p_155551_);
    }

    public void load(CompoundTag p_155588_)
    {
        super.load(p_155588_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        if (!this.tryLoadLootTable(p_155588_))
        {
            ContainerHelper.loadAllItems(p_155588_, this.items);
        }

        this.cooldownTime = p_155588_.getInt("TransferCooldown");
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);

        if (!this.trySaveLootTable(pCompound))
        {
            ContainerHelper.saveAllItems(pCompound, this.items);
        }

        pCompound.putInt("TransferCooldown", this.cooldownTime);
        return pCompound;
    }

    public int getContainerSize()
    {
        return this.items.size();
    }

    public ItemStack removeItem(int pIndex, int pCount)
    {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
    }

    public void setItem(int pIndex, ItemStack pStack)
    {
        this.unpackLootTable((Player)null);
        this.getItems().set(pIndex, pStack);

        if (pStack.getCount() > this.getMaxStackSize())
        {
            pStack.setCount(this.getMaxStackSize());
        }
    }

    protected Component getDefaultName()
    {
        return new TranslatableComponent("container.hopper");
    }

    public static void pushItemsTick(Level p_155574_, BlockPos p_155575_, BlockState p_155576_, HopperBlockEntity p_155577_)
    {
        --p_155577_.cooldownTime;
        p_155577_.tickedGameTime = p_155574_.getGameTime();

        if (!p_155577_.isOnCooldown())
        {
            p_155577_.setCooldown(0);
            tryMoveItems(p_155574_, p_155575_, p_155576_, p_155577_, () ->
            {
                return suckInItems(p_155574_, p_155577_);
            });
        }
    }

    private static boolean tryMoveItems(Level p_155579_, BlockPos p_155580_, BlockState p_155581_, HopperBlockEntity p_155582_, BooleanSupplier p_155583_)
    {
        if (p_155579_.isClientSide)
        {
            return false;
        }
        else
        {
            if (!p_155582_.isOnCooldown() && p_155581_.getValue(HopperBlock.ENABLED))
            {
                boolean flag = false;

                if (!p_155582_.isEmpty())
                {
                    flag = ejectItems(p_155579_, p_155580_, p_155581_, p_155582_);
                }

                if (!p_155582_.inventoryFull())
                {
                    flag |= p_155583_.getAsBoolean();
                }

                if (flag)
                {
                    p_155582_.setCooldown(8);
                    setChanged(p_155579_, p_155580_, p_155581_);
                    return true;
                }
            }

            return false;
        }
    }

    private boolean inventoryFull()
    {
        for (ItemStack itemstack : this.items)
        {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize())
            {
                return false;
            }
        }

        return true;
    }

    private static boolean ejectItems(Level p_155563_, BlockPos p_155564_, BlockState p_155565_, Container p_155566_)
    {
        Container container = getAttachedContainer(p_155563_, p_155564_, p_155565_);

        if (container == null)
        {
            return false;
        }
        else
        {
            Direction direction = p_155565_.getValue(HopperBlock.FACING).getOpposite();

            if (isFullContainer(container, direction))
            {
                return false;
            }
            else
            {
                for (int i = 0; i < p_155566_.getContainerSize(); ++i)
                {
                    if (!p_155566_.getItem(i).isEmpty())
                    {
                        ItemStack itemstack = p_155566_.getItem(i).copy();
                        ItemStack itemstack1 = addItem(p_155566_, container, p_155566_.removeItem(i, 1), direction);

                        if (itemstack1.isEmpty())
                        {
                            container.setChanged();
                            return true;
                        }

                        p_155566_.setItem(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    private static IntStream getSlots(Container p_59340_, Direction p_59341_)
    {
        return p_59340_ instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)p_59340_).getSlotsForFace(p_59341_)) : IntStream.range(0, p_59340_.getContainerSize());
    }

    private static boolean isFullContainer(Container p_59386_, Direction pInventory)
    {
        return getSlots(p_59386_, pInventory).allMatch((p_59379_) ->
        {
            ItemStack itemstack = p_59386_.getItem(p_59379_);
            return itemstack.getCount() >= itemstack.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container pInventory, Direction pSide)
    {
        return getSlots(pInventory, pSide).allMatch((p_59319_) ->
        {
            return pInventory.getItem(p_59319_).isEmpty();
        });
    }

    public static boolean suckInItems(Level p_155553_, Hopper p_155554_)
    {
        Container container = getSourceContainer(p_155553_, p_155554_);

        if (container != null)
        {
            Direction direction = Direction.DOWN;
            return isEmptyContainer(container, direction) ? false : getSlots(container, direction).anyMatch((p_59363_) ->
            {
                return tryTakeInItemFromSlot(p_155554_, container, p_59363_, direction);
            });
        }
        else
        {
            for (ItemEntity itementity : getItemsAtAndAbove(p_155553_, p_155554_))
            {
                if (addItem(p_155554_, itementity))
                {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(Hopper pHopper, Container pInventory, int pIndex, Direction pDirection)
    {
        ItemStack itemstack = pInventory.getItem(pIndex);

        if (!itemstack.isEmpty() && canTakeItemFromContainer(pInventory, itemstack, pIndex, pDirection))
        {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = addItem(pInventory, pHopper, pInventory.removeItem(pIndex, 1), (Direction)null);

            if (itemstack2.isEmpty())
            {
                pInventory.setChanged();
                return true;
            }

            pInventory.setItem(pIndex, itemstack1);
        }

        return false;
    }

    public static boolean addItem(Container pSource, ItemEntity pDestination)
    {
        boolean flag = false;
        ItemStack itemstack = pDestination.getItem().copy();
        ItemStack itemstack1 = addItem((Container)null, pSource, itemstack, (Direction)null);

        if (itemstack1.isEmpty())
        {
            flag = true;
            pDestination.discard();
        }
        else
        {
            pDestination.setItem(itemstack1);
        }

        return flag;
    }

    public static ItemStack addItem(@Nullable Container pSource, Container pDestination, ItemStack pStack, @Nullable Direction pDirection)
    {
        if (pDestination instanceof WorldlyContainer && pDirection != null)
        {
            WorldlyContainer worldlycontainer = (WorldlyContainer)pDestination;
            int[] aint = worldlycontainer.getSlotsForFace(pDirection);

            for (int k = 0; k < aint.length && !pStack.isEmpty(); ++k)
            {
                pStack = tryMoveInItem(pSource, pDestination, pStack, aint[k], pDirection);
            }
        }
        else
        {
            int i = pDestination.getContainerSize();

            for (int j = 0; j < i && !pStack.isEmpty(); ++j)
            {
                pStack = tryMoveInItem(pSource, pDestination, pStack, j, pDirection);
            }
        }

        return pStack;
    }

    private static boolean canPlaceItemInContainer(Container pInventory, ItemStack pStack, int pIndex, @Nullable Direction pSide)
    {
        if (!pInventory.canPlaceItem(pIndex, pStack))
        {
            return false;
        }
        else
        {
            return !(pInventory instanceof WorldlyContainer) || ((WorldlyContainer)pInventory).canPlaceItemThroughFace(pIndex, pStack, pSide);
        }
    }

    private static boolean canTakeItemFromContainer(Container pInventory, ItemStack pStack, int pIndex, Direction pSide)
    {
        return !(pInventory instanceof WorldlyContainer) || ((WorldlyContainer)pInventory).canTakeItemThroughFace(pIndex, pStack, pSide);
    }

    private static ItemStack tryMoveInItem(@Nullable Container pSource, Container pDestination, ItemStack pStack, int pIndex, @Nullable Direction pDirection)
    {
        ItemStack itemstack = pDestination.getItem(pIndex);

        if (canPlaceItemInContainer(pDestination, pStack, pIndex, pDirection))
        {
            boolean flag = false;
            boolean flag1 = pDestination.isEmpty();

            if (itemstack.isEmpty())
            {
                pDestination.setItem(pIndex, pStack);
                pStack = ItemStack.EMPTY;
                flag = true;
            }
            else if (canMergeItems(itemstack, pStack))
            {
                int i = pStack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(pStack.getCount(), i);
                pStack.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag)
            {
                if (flag1 && pDestination instanceof HopperBlockEntity)
                {
                    HopperBlockEntity hopperblockentity1 = (HopperBlockEntity)pDestination;

                    if (!hopperblockentity1.isOnCustomCooldown())
                    {
                        int k = 0;

                        if (pSource instanceof HopperBlockEntity)
                        {
                            HopperBlockEntity hopperblockentity = (HopperBlockEntity)pSource;

                            if (hopperblockentity1.tickedGameTime >= hopperblockentity.tickedGameTime)
                            {
                                k = 1;
                            }
                        }

                        hopperblockentity1.setCooldown(8 - k);
                    }
                }

                pDestination.setChanged();
            }
        }

        return pStack;
    }

    @Nullable
    private static Container getAttachedContainer(Level p_155593_, BlockPos p_155594_, BlockState p_155595_)
    {
        Direction direction = p_155595_.getValue(HopperBlock.FACING);
        return getContainerAt(p_155593_, p_155594_.relative(direction));
    }

    @Nullable
    private static Container getSourceContainer(Level p_155597_, Hopper p_155598_)
    {
        return getContainerAt(p_155597_, p_155598_.getLevelX(), p_155598_.getLevelY() + 1.0D, p_155598_.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level p_155590_, Hopper p_155591_)
    {
        return p_155591_.getSuckShape().toAabbs().stream().flatMap((p_155558_) ->
        {
            return p_155590_.getEntitiesOfClass(ItemEntity.class, p_155558_.move(p_155591_.getLevelX() - 0.5D, p_155591_.getLevelY() - 0.5D, p_155591_.getLevelZ() - 0.5D), EntitySelector.ENTITY_STILL_ALIVE).stream();
        }).collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level pLevel, BlockPos pX)
    {
        return getContainerAt(pLevel, (double)pX.getX() + 0.5D, (double)pX.getY() + 0.5D, (double)pX.getZ() + 0.5D);
    }

    @Nullable
    private static Container getContainerAt(Level pLevel, double pX, double p_59350_, double pY)
    {
        Container container = null;
        BlockPos blockpos = new BlockPos(pX, p_59350_, pY);
        BlockState blockstate = pLevel.getBlockState(blockpos);
        Block block = blockstate.getBlock();

        if (block instanceof WorldlyContainerHolder)
        {
            container = ((WorldlyContainerHolder)block).getContainer(blockstate, pLevel, blockpos);
        }
        else if (blockstate.hasBlockEntity())
        {
            BlockEntity blockentity = pLevel.getBlockEntity(blockpos);

            if (blockentity instanceof Container)
            {
                container = (Container)blockentity;

                if (container instanceof ChestBlockEntity && block instanceof ChestBlock)
                {
                    container = ChestBlock.getContainer((ChestBlock)block, blockstate, pLevel, blockpos, true);
                }
            }
        }

        if (container == null)
        {
            List<Entity> list = pLevel.getEntities((Entity)null, new AABB(pX - 0.5D, p_59350_ - 0.5D, pY - 0.5D, pX + 0.5D, p_59350_ + 0.5D, pY + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);

            if (!list.isEmpty())
            {
                container = (Container)list.get(pLevel.random.nextInt(list.size()));
            }
        }

        return container;
    }

    private static boolean canMergeItems(ItemStack pStack1, ItemStack pStack2)
    {
        if (!pStack1.is(pStack2.getItem()))
        {
            return false;
        }
        else if (pStack1.getDamageValue() != pStack2.getDamageValue())
        {
            return false;
        }
        else if (pStack1.getCount() > pStack1.getMaxStackSize())
        {
            return false;
        }
        else
        {
            return ItemStack.tagMatches(pStack1, pStack2);
        }
    }

    public double getLevelX()
    {
        return (double)this.worldPosition.getX() + 0.5D;
    }

    public double getLevelY()
    {
        return (double)this.worldPosition.getY() + 0.5D;
    }

    public double getLevelZ()
    {
        return (double)this.worldPosition.getZ() + 0.5D;
    }

    private void setCooldown(int pTicks)
    {
        this.cooldownTime = pTicks;
    }

    private boolean isOnCooldown()
    {
        return this.cooldownTime > 0;
    }

    private boolean isOnCustomCooldown()
    {
        return this.cooldownTime > 8;
    }

    protected NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> pItems)
    {
        this.items = pItems;
    }

    public static void entityInside(Level p_155568_, BlockPos p_155569_, BlockState p_155570_, Entity p_155571_, HopperBlockEntity p_155572_)
    {
        if (p_155571_ instanceof ItemEntity && Shapes.joinIsNotEmpty(Shapes.create(p_155571_.getBoundingBox().move((double)(-p_155569_.getX()), (double)(-p_155569_.getY()), (double)(-p_155569_.getZ()))), p_155572_.getSuckShape(), BooleanOp.AND))
        {
            tryMoveItems(p_155568_, p_155569_, p_155570_, p_155572_, () ->
            {
                return addItem(p_155572_, (ItemEntity)p_155571_);
            });
        }
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer)
    {
        return new HopperMenu(pId, pPlayer, this);
    }
}
