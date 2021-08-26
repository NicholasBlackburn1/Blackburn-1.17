package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener>
{
    private final int id;

    public ServerboundAcceptTeleportationPacket(int p_133788_)
    {
        this.id = p_133788_;
    }

    public ServerboundAcceptTeleportationPacket(FriendlyByteBuf p_179538_)
    {
        this.id = p_179538_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.id);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleAcceptTeleportPacket(this);
    }

    public int getId()
    {
        return this.id;
    }
}
