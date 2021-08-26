package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener>
{
    private final int slot;

    public ServerboundSetCarriedItemPacket(int p_134491_)
    {
        this.slot = p_134491_;
    }

    public ServerboundSetCarriedItemPacket(FriendlyByteBuf p_179751_)
    {
        this.slot = p_179751_.readShort();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeShort(this.slot);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleSetCarriedItem(this);
    }

    public int getSlot()
    {
        return this.slot;
    }
}
