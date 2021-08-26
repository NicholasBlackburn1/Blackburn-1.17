package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener>
{
    private final int sourceId;
    private final int destId;

    public ClientboundSetEntityLinkPacket(Entity p_133164_, @Nullable Entity p_133165_)
    {
        this.sourceId = p_133164_.getId();
        this.destId = p_133165_ != null ? p_133165_.getId() : 0;
    }

    public ClientboundSetEntityLinkPacket(FriendlyByteBuf p_179292_)
    {
        this.sourceId = p_179292_.readInt();
        this.destId = p_179292_.readInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeInt(this.sourceId);
        pBuf.writeInt(this.destId);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleEntityLinkPacket(this);
    }

    public int getSourceId()
    {
        return this.sourceId;
    }

    public int getDestId()
    {
        return this.destId;
    }
}
