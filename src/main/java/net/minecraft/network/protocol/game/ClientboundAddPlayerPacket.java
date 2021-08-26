package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener>
{
    private final int entityId;
    private final UUID playerId;
    private final double x;
    private final double y;
    private final double z;
    private final byte yRot;
    private final byte xRot;

    public ClientboundAddPlayerPacket(Player p_131596_)
    {
        this.entityId = p_131596_.getId();
        this.playerId = p_131596_.getGameProfile().getId();
        this.x = p_131596_.getX();
        this.y = p_131596_.getY();
        this.z = p_131596_.getZ();
        this.yRot = (byte)((int)(p_131596_.getYRot() * 256.0F / 360.0F));
        this.xRot = (byte)((int)(p_131596_.getXRot() * 256.0F / 360.0F));
    }

    public ClientboundAddPlayerPacket(FriendlyByteBuf p_178570_)
    {
        this.entityId = p_178570_.readVarInt();
        this.playerId = p_178570_.readUUID();
        this.x = p_178570_.readDouble();
        this.y = p_178570_.readDouble();
        this.z = p_178570_.readDouble();
        this.yRot = p_178570_.readByte();
        this.xRot = p_178570_.readByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.entityId);
        pBuf.writeUUID(this.playerId);
        pBuf.writeDouble(this.x);
        pBuf.writeDouble(this.y);
        pBuf.writeDouble(this.z);
        pBuf.writeByte(this.yRot);
        pBuf.writeByte(this.xRot);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleAddPlayer(this);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public UUID getPlayerId()
    {
        return this.playerId;
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

    public byte getyRot()
    {
        return this.yRot;
    }

    public byte getxRot()
    {
        return this.xRot;
    }
}
