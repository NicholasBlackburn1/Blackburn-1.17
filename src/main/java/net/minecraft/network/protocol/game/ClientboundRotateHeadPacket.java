package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket implements Packet<ClientGamePacketListener>
{
    private final int entityId;
    private final byte yHeadRot;

    public ClientboundRotateHeadPacket(Entity p_132967_, byte p_132968_)
    {
        this.entityId = p_132967_.getId();
        this.yHeadRot = p_132968_;
    }

    public ClientboundRotateHeadPacket(FriendlyByteBuf p_179193_)
    {
        this.entityId = p_179193_.readVarInt();
        this.yHeadRot = p_179193_.readByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.entityId);
        pBuf.writeByte(this.yHeadRot);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleRotateMob(this);
    }

    public Entity getEntity(Level pLevel)
    {
        return pLevel.getEntity(this.entityId);
    }

    public byte getYHeadRot()
    {
        return this.yHeadRot;
    }
}
