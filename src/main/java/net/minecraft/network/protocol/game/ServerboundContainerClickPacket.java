package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener>
{
    private static final int f_182731_ = 128;
    private final int containerId;
    private final int f_182732_;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final ItemStack carriedItem;
    private final Int2ObjectMap<ItemStack> changedSlots;

    public ServerboundContainerClickPacket(int p_182734_, int p_182735_, int p_182736_, int p_182737_, ClickType p_182738_, ItemStack p_182739_, Int2ObjectMap<ItemStack> p_182740_)
    {
        this.containerId = p_182734_;
        this.f_182732_ = p_182735_;
        this.slotNum = p_182736_;
        this.buttonNum = p_182737_;
        this.clickType = p_182738_;
        this.carriedItem = p_182739_;
        this.changedSlots = Int2ObjectMaps.unmodifiable(p_182740_);
    }

    public ServerboundContainerClickPacket(FriendlyByteBuf p_179578_)
    {
        this.containerId = p_179578_.readByte();
        this.f_182732_ = p_179578_.readVarInt();
        this.slotNum = p_179578_.readShort();
        this.buttonNum = p_179578_.readByte();
        this.clickType = p_179578_.readEnum(ClickType.class);
        this.changedSlots = Int2ObjectMaps.unmodifiable(p_179578_.readMap(FriendlyByteBuf.m_182695_(Int2ObjectOpenHashMap::new, 128), (p_179580_) ->
        {
            return Integer.valueOf(p_179580_.readShort());
        }, FriendlyByteBuf::readItem));
        this.carriedItem = p_179578_.readItem();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
        pBuf.writeVarInt(this.f_182732_);
        pBuf.writeShort(this.slotNum);
        pBuf.writeByte(this.buttonNum);
        pBuf.writeEnum(this.clickType);
        pBuf.writeMap(this.changedSlots, FriendlyByteBuf::writeShort, FriendlyByteBuf::writeItem);
        pBuf.writeItem(this.carriedItem);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleContainerClick(this);
    }

    public int getContainerId()
    {
        return this.containerId;
    }

    public int getSlotNum()
    {
        return this.slotNum;
    }

    public int getButtonNum()
    {
        return this.buttonNum;
    }

    public ItemStack getCarriedItem()
    {
        return this.carriedItem;
    }

    public Int2ObjectMap<ItemStack> getChangedSlots()
    {
        return this.changedSlots;
    }

    public ClickType getClickType()
    {
        return this.clickType;
    }

    public int m_182741_()
    {
        return this.f_182732_;
    }
}
