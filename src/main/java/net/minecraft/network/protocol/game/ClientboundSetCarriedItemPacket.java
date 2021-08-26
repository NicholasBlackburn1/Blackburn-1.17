package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener>
{
    private final int slot;

    public ClientboundSetCarriedItemPacket(int p_133072_)
    {
        this.slot = p_133072_;
    }

    public ClientboundSetCarriedItemPacket(FriendlyByteBuf p_179280_)
    {
        this.slot = p_179280_.readByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.slot);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetCarriedItem(this);
    }

    public int getSlot()
    {
        return this.slot;
    }
}
