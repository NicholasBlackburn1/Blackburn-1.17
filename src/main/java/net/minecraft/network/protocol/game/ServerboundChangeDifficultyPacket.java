package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener>
{
    private final Difficulty difficulty;

    public ServerboundChangeDifficultyPacket(Difficulty p_133817_)
    {
        this.difficulty = p_133817_;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleChangeDifficulty(this);
    }

    public ServerboundChangeDifficultyPacket(FriendlyByteBuf p_179542_)
    {
        this.difficulty = Difficulty.byId(p_179542_.readUnsignedByte());
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.difficulty.getId());
    }

    public Difficulty getDifficulty()
    {
        return this.difficulty;
    }
}
