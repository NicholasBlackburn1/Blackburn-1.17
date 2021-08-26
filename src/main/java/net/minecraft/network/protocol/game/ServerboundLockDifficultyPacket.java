package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener>
{
    private final boolean locked;

    public ServerboundLockDifficultyPacket(boolean p_134108_)
    {
        this.locked = p_134108_;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleLockDifficulty(this);
    }

    public ServerboundLockDifficultyPacket(FriendlyByteBuf p_179673_)
    {
        this.locked = p_179673_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBoolean(this.locked);
    }

    public boolean isLocked()
    {
        return this.locked;
    }
}
