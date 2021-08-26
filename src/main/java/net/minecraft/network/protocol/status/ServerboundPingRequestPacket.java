package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener>
{
    private final long time;

    public ServerboundPingRequestPacket(long p_134991_)
    {
        this.time = p_134991_;
    }

    public ServerboundPingRequestPacket(FriendlyByteBuf p_179838_)
    {
        this.time = p_179838_.readLong();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeLong(this.time);
    }

    public void handle(ServerStatusPacketListener pHandler)
    {
        pHandler.handlePingRequest(this);
    }

    public long getTime()
    {
        return this.time;
    }
}
