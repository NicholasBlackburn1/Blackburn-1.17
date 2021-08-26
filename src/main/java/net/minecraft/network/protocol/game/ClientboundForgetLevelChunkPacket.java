package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener>
{
    private final int x;
    private final int z;

    public ClientboundForgetLevelChunkPacket(int p_132141_, int p_132142_)
    {
        this.x = p_132141_;
        this.z = p_132142_;
    }

    public ClientboundForgetLevelChunkPacket(FriendlyByteBuf p_178858_)
    {
        this.x = p_178858_.readInt();
        this.z = p_178858_.readInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeInt(this.x);
        pBuf.writeInt(this.z);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleForgetLevelChunk(this);
    }

    public int getX()
    {
        return this.x;
    }

    public int getZ()
    {
        return this.z;
    }
}
