package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener>
{
    private final boolean left;
    private final boolean right;

    public ServerboundPaddleBoatPacket(boolean p_134210_, boolean p_134211_)
    {
        this.left = p_134210_;
        this.right = p_134211_;
    }

    public ServerboundPaddleBoatPacket(FriendlyByteBuf p_179702_)
    {
        this.left = p_179702_.readBoolean();
        this.right = p_179702_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBoolean(this.left);
        pBuf.writeBoolean(this.right);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handlePaddleBoat(this);
    }

    public boolean getLeft()
    {
        return this.left;
    }

    public boolean getRight()
    {
        return this.right;
    }
}
