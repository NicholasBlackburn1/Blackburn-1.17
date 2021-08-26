package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener>
{
    private final long id;

    public ServerboundKeepAlivePacket(long p_134095_)
    {
        this.id = p_134095_;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleKeepAlive(this);
    }

    public ServerboundKeepAlivePacket(FriendlyByteBuf p_179671_)
    {
        this.id = p_179671_.readLong();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeLong(this.id);
    }

    public long getId()
    {
        return this.id;
    }
}
