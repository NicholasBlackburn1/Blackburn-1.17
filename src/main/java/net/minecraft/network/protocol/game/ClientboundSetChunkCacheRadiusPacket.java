package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetChunkCacheRadiusPacket implements Packet<ClientGamePacketListener>
{
    private final int radius;

    public ClientboundSetChunkCacheRadiusPacket(int p_133101_)
    {
        this.radius = p_133101_;
    }

    public ClientboundSetChunkCacheRadiusPacket(FriendlyByteBuf p_179284_)
    {
        this.radius = p_179284_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.radius);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetChunkCacheRadius(this);
    }

    public int getRadius()
    {
        return this.radius;
    }
}
