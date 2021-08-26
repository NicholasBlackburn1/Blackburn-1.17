package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener>
{
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final int value;

    public ClientboundAddExperienceOrbPacket(ExperienceOrb p_131517_)
    {
        this.id = p_131517_.getId();
        this.x = p_131517_.getX();
        this.y = p_131517_.getY();
        this.z = p_131517_.getZ();
        this.value = p_131517_.getValue();
    }

    public ClientboundAddExperienceOrbPacket(FriendlyByteBuf p_178564_)
    {
        this.id = p_178564_.readVarInt();
        this.x = p_178564_.readDouble();
        this.y = p_178564_.readDouble();
        this.z = p_178564_.readDouble();
        this.value = p_178564_.readShort();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.id);
        pBuf.writeDouble(this.x);
        pBuf.writeDouble(this.y);
        pBuf.writeDouble(this.z);
        pBuf.writeShort(this.value);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleAddExperienceOrb(this);
    }

    public int getId()
    {
        return this.id;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public int getValue()
    {
        return this.value;
    }
}
