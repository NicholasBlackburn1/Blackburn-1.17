package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener>
{
    private final int containerId;

    public ServerboundContainerClosePacket(int p_133970_)
    {
        this.containerId = p_133970_;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleContainerClose(this);
    }

    public ServerboundContainerClosePacket(FriendlyByteBuf p_179584_)
    {
        this.containerId = p_179584_.readByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
    }

    public int getContainerId()
    {
        return this.containerId;
    }
}
