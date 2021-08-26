package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener>
{
    private final float experienceProgress;
    private final int totalExperience;
    private final int experienceLevel;

    public ClientboundSetExperiencePacket(float p_133219_, int p_133220_, int p_133221_)
    {
        this.experienceProgress = p_133219_;
        this.totalExperience = p_133220_;
        this.experienceLevel = p_133221_;
    }

    public ClientboundSetExperiencePacket(FriendlyByteBuf p_179299_)
    {
        this.experienceProgress = p_179299_.readFloat();
        this.experienceLevel = p_179299_.readVarInt();
        this.totalExperience = p_179299_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeFloat(this.experienceProgress);
        pBuf.writeVarInt(this.experienceLevel);
        pBuf.writeVarInt(this.totalExperience);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetExperience(this);
    }

    public float getExperienceProgress()
    {
        return this.experienceProgress;
    }

    public int getTotalExperience()
    {
        return this.totalExperience;
    }

    public int getExperienceLevel()
    {
        return this.experienceLevel;
    }
}
