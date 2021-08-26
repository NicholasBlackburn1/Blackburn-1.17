package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener>
{
    private final int itemId;
    private final int playerId;
    private final int amount;

    public ClientboundTakeItemEntityPacket(int p_133515_, int p_133516_, int p_133517_)
    {
        this.itemId = p_133515_;
        this.playerId = p_133516_;
        this.amount = p_133517_;
    }

    public ClientboundTakeItemEntityPacket(FriendlyByteBuf p_179435_)
    {
        this.itemId = p_179435_.readVarInt();
        this.playerId = p_179435_.readVarInt();
        this.amount = p_179435_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.itemId);
        pBuf.writeVarInt(this.playerId);
        pBuf.writeVarInt(this.amount);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleTakeItemEntity(this);
    }

    public int getItemId()
    {
        return this.itemId;
    }

    public int getPlayerId()
    {
        return this.playerId;
    }

    public int getAmount()
    {
        return this.amount;
    }
}
