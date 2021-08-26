package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener>
{
    private final int containerId;
    private final int size;
    private final int entityId;

    public ClientboundHorseScreenOpenPacket(int p_132195_, int p_132196_, int p_132197_)
    {
        this.containerId = p_132195_;
        this.size = p_132196_;
        this.entityId = p_132197_;
    }

    public ClientboundHorseScreenOpenPacket(FriendlyByteBuf p_178867_)
    {
        this.containerId = p_178867_.readUnsignedByte();
        this.size = p_178867_.readVarInt();
        this.entityId = p_178867_.readInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
        pBuf.writeVarInt(this.size);
        pBuf.writeInt(this.entityId);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleHorseScreenOpen(this);
    }

    public int getContainerId()
    {
        return this.containerId;
    }

    public int getSize()
    {
        return this.size;
    }

    public int getEntityId()
    {
        return this.entityId;
    }
}
