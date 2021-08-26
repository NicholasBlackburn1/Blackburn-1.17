package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener>
{
    private final int entityId;
    private final byte eventId;

    public ClientboundEntityEventPacket(Entity p_132092_, byte p_132093_)
    {
        this.entityId = p_132092_.getId();
        this.eventId = p_132093_;
    }

    public ClientboundEntityEventPacket(FriendlyByteBuf p_178843_)
    {
        this.entityId = p_178843_.readInt();
        this.eventId = p_178843_.readByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeInt(this.entityId);
        pBuf.writeByte(this.eventId);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleEntityEvent(this);
    }

    @Nullable
    public Entity getEntity(Level pLevel)
    {
        return pLevel.getEntity(this.entityId);
    }

    public byte getEventId()
    {
        return this.eventId;
    }
}
