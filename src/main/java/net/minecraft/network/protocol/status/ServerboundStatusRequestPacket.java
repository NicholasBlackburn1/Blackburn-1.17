package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener>
{
    public ServerboundStatusRequestPacket()
    {
    }

    public ServerboundStatusRequestPacket(FriendlyByteBuf p_179840_)
    {
    }

    public void write(FriendlyByteBuf pBuf)
    {
    }

    public void handle(ServerStatusPacketListener pHandler)
    {
        pHandler.handleStatusRequest(this);
    }
}
