package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener>
{
    private final float health;
    private final int food;
    private final float saturation;

    public ClientboundSetHealthPacket(float p_133238_, int p_133239_, float p_133240_)
    {
        this.health = p_133238_;
        this.food = p_133239_;
        this.saturation = p_133240_;
    }

    public ClientboundSetHealthPacket(FriendlyByteBuf p_179301_)
    {
        this.health = p_179301_.readFloat();
        this.food = p_179301_.readVarInt();
        this.saturation = p_179301_.readFloat();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeFloat(this.health);
        pBuf.writeVarInt(this.food);
        pBuf.writeFloat(this.saturation);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetHealth(this);
    }

    public float getHealth()
    {
        return this.health;
    }

    public int getFood()
    {
        return this.food;
    }

    public float getSaturation()
    {
        return this.saturation;
    }
}
