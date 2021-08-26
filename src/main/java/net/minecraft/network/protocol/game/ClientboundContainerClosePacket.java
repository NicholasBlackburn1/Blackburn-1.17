package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener>
{
    private final int containerId;

    public ClientboundContainerClosePacket(int p_131933_)
    {
        this.containerId = p_131933_;
    }

    public ClientboundContainerClosePacket(FriendlyByteBuf p_178820_)
    {
        this.containerId = p_178820_.readUnsignedByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleContainerClose(this);
    }

    public int getContainerId()
    {
        return this.containerId;
    }
}
