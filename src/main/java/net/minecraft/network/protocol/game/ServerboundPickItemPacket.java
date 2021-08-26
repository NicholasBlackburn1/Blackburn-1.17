package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPickItemPacket implements Packet<ServerGamePacketListener>
{
    private final int slot;

    public ServerboundPickItemPacket(int p_134225_)
    {
        this.slot = p_134225_;
    }

    public ServerboundPickItemPacket(FriendlyByteBuf p_179704_)
    {
        this.slot = p_179704_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.slot);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handlePickItem(this);
    }

    public int getSlot()
    {
        return this.slot;
    }
}
