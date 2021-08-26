package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener>
{
    private final long time;

    public ClientboundPongResponsePacket(long p_134876_)
    {
        this.time = p_134876_;
    }

    public ClientboundPongResponsePacket(FriendlyByteBuf p_179831_)
    {
        this.time = p_179831_.readLong();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeLong(this.time);
    }

    public void handle(ClientStatusPacketListener pHandler)
    {
        pHandler.handlePongResponse(this);
    }

    public long getTime()
    {
        return this.time;
    }
}
