package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity
{
    private static final int EVENT_SET_OPEN_COUNT = 1;
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter()
    {
        protected void onOpen(Level p_155357_, BlockPos p_155358_, BlockState p_155359_)
        {
            ChestBlockEntity.playSound(p_155357_, p_155358_, p_155359_, SoundEvents.CHEST_OPEN);
        }
        protected void onClose(Level p_155367_, BlockPos p_155368_, BlockState p_155369_)
        {
            ChestBlockEntity.playSound(p_155367_, p_155368_, p_155369_, SoundEvents.CHEST_CLOSE);
        }
        protected void openerCountChanged(Level p_155361_, BlockPos p_155362_, BlockState p_155363_, int p_155364_, int p_155365_)
        {
            ChestBlockEntity.this.signalOpenCount(p_155361_, p_155362_, p_155363_, p_155364_, p_155365_);
        }
        protected boolean isOwnContainer(Player p_155355_)
        {
            if (!(p_155355_.containerMenu instanceof ChestMenu))
            {
                return false;
            }
            else
            {
                Container container = ((ChestMenu)p_155355_.containerMenu).getContainer();
                return container == ChestBlockEntity.this || container instanceof CompoundContainer && ((CompoundContainer)container).contains(ChestBlockEntity.this);
            }
        }
    };
    private final ChestLidController chestLidController = new ChestLidController();

    protected ChestBlockEntity(BlockEntityType<?> p_155327_, BlockPos p_155328_, BlockState p_155329_)
    {
        super(p_155327_, p_155328_, p_155329_);
    }

    public ChestBlockEntity(BlockPos p_155331_, BlockState p_155332_)
    {
        this(BlockEntityType.CHEST, p_155331_, p_155332_);
    }

    public int getContainerSize()
    {
        return 27;
    }

    protected Component getDefaultName()
    {
        return new TranslatableComponent("container.chest");
    }

    public void load(CompoundTag p_155349_)
    {
        super.load(p_155349_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        if (!this.tryLoadLootTable(p_155349_))
        {
            ContainerHelper.loadAllItems(p_155349_, this.items);
        }
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);

        if (!this.trySaveLootTable(pCompound))
        {
            ContainerHelper.saveAllItems(pCompound, this.items);
        }

        return pCompound;
    }

    public static void lidAnimateTick(Level p_155344_, BlockPos p_155345_, BlockState p_155346_, ChestBlockEntity p_155347_)
    {
        p_155347_.chestLidController.tickLid();
    }

    static void playSound(Level p_155339_, BlockPos p_155340_, BlockState p_155341_, SoundEvent p_155342_)
    {
        ChestType chesttype = p_155341_.getValue(ChestBlock.TYPE);

        if (chesttype != ChestType.LEFT)
        {
            double d0 = (double)p_155340_.getX() + 0.5D;
            double d1 = (double)p_155340_.getY() + 0.5D;
            double d2 = (double)p_155340_.getZ() + 0.5D;

            if (chesttype == ChestType.RIGHT)
            {
                Direction direction = ChestBlock.getConnectedDirection(p_155341_);
                d0 += (double)direction.getStepX() * 0.5D;
                d2 += (double)direction.getStepZ() * 0.5D;
            }

            p_155339_.playSound((Player)null, d0, d1, d2, p_155342_, SoundSource.BLOCKS, 0.5F, p_155339_.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public boolean triggerEvent(int pId, int pType)
    {
        if (pId == 1)
        {
            this.chestLidController.shouldBeOpen(pType > 0);
            return true;
        }
        else
        {
            return super.triggerEvent(pId, pType);
        }
    }

    public void startOpen(Player pPlayer)
    {
        if (!this.remove && !pPlayer.isSpectator())
        {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player pPlayer)
    {
        if (!this.remove && !pPlayer.isSpectator())
        {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> pItems)
    {
        this.items = pItems;
    }

    public float getOpenNess(float pPartialTicks)
    {
        return this.chestLidController.getOpenness(pPartialTicks);
    }

    public static int getOpenCount(BlockGetter pReader, BlockPos pPos)
    {
        BlockState blockstate = pReader.getBlockState(pPos);

        if (blockstate.hasBlockEntity())
        {
            BlockEntity blockentity = pReader.getBlockEntity(pPos);

            if (blockentity instanceof ChestBlockEntity)
            {
                return ((ChestBlockEntity)blockentity).openersCounter.getOpenerCount();
            }
        }

        return 0;
    }

    public static void swapContents(ChestBlockEntity pChest, ChestBlockEntity pOtherChest)
    {
        NonNullList<ItemStack> nonnulllist = pChest.getItems();
        pChest.setItems(pOtherChest.getItems());
        pOtherChest.setItems(nonnulllist);
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer)
    {
        return ChestMenu.threeRows(pId, pPlayer, this);
    }

    public void recheckOpen()
    {
        if (!this.remove)
        {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected void signalOpenCount(Level p_155333_, BlockPos p_155334_, BlockState p_155335_, int p_155336_, int p_155337_)
    {
        Block block = p_155335_.getBlock();
        p_155333_.blockEvent(p_155334_, block, 1, p_155337_);
    }
}
