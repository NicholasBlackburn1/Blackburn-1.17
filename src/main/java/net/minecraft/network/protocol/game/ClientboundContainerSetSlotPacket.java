package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener>
{
    public static final int CARRIED_ITEM = -1;
    public static final int PLAYER_INVENTORY = -2;
    private final int containerId;
    private final int f_182710_;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int p_131982_, int p_182713_, int p_131983_, ItemStack p_131984_)
    {
        this.containerId = p_131982_;
        this.f_182710_ = p_182713_;
        this.slot = p_131983_;
        this.itemStack = p_131984_.copy();
    }

    public ClientboundContainerSetSlotPacket(FriendlyByteBuf p_178829_)
    {
        this.containerId = p_178829_.readByte();
        this.f_182710_ = p_178829_.readVarInt();
        this.slot = p_178829_.readShort();
        this.itemStack = p_178829_.readItem();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
        pBuf.writeVarInt(this.f_182710_);
        pBuf.writeShort(this.slot);
        pBuf.writeItem(this.itemStack);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleContainerSetSlot(this);
    }

    public int getContainerId()
    {
        return this.containerId;
    }

    public int getSlot()
    {
        return this.slot;
    }

    public ItemStack getItem()
    {
        return this.itemStack;
    }

    public int m_182716_()
    {
        return this.f_182710_;
    }
}
