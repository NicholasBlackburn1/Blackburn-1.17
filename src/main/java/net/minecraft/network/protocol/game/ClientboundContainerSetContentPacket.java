package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener>
{
    private final int containerId;
    private final int f_182701_;
    private final List<ItemStack> items;
    private final ItemStack f_182702_;

    public ClientboundContainerSetContentPacket(int p_182704_, int p_182705_, NonNullList<ItemStack> p_182706_, ItemStack p_182707_)
    {
        this.containerId = p_182704_;
        this.f_182701_ = p_182705_;
        this.items = NonNullList.withSize(p_182706_.size(), ItemStack.EMPTY);

        for (int i = 0; i < p_182706_.size(); ++i)
        {
            this.items.set(i, p_182706_.get(i).copy());
        }

        this.f_182702_ = p_182707_.copy();
    }

    public ClientboundContainerSetContentPacket(FriendlyByteBuf p_178823_)
    {
        this.containerId = p_178823_.readUnsignedByte();
        this.f_182701_ = p_178823_.readVarInt();
        this.items = p_178823_.readCollection(NonNullList::m_182647_, FriendlyByteBuf::readItem);
        this.f_182702_ = p_178823_.readItem();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
        pBuf.writeVarInt(this.f_182701_);
        pBuf.writeCollection(this.items, FriendlyByteBuf::writeItem);
        pBuf.writeItem(this.f_182702_);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleContainerContent(this);
    }

    public int getContainerId()
    {
        return this.containerId;
    }

    public List<ItemStack> getItems()
    {
        return this.items;
    }

    public ItemStack m_182708_()
    {
        return this.f_182702_;
    }

    public int m_182709_()
    {
        return this.f_182701_;
    }
}
