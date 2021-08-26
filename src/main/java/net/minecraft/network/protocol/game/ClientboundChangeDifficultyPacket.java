package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener>
{
    private final Difficulty difficulty;
    private final boolean locked;

    public ClientboundChangeDifficultyPacket(Difficulty p_131809_, boolean p_131810_)
    {
        this.difficulty = p_131809_;
        this.locked = p_131810_;
    }

    public ClientboundChangeDifficultyPacket(FriendlyByteBuf p_178774_)
    {
        this.difficulty = Difficulty.byId(p_178774_.readUnsignedByte());
        this.locked = p_178774_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.difficulty.getId());
        pBuf.writeBoolean(this.locked);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleChangeDifficulty(this);
    }

    public boolean isLocked()
    {
        return this.locked;
    }

    public Difficulty getDifficulty()
    {
        return this.difficulty;
    }
}
