package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetDefaultSpawnPositionPacket implements Packet<ClientGamePacketListener>
{
    private final BlockPos pos;
    private final float angle;

    public ClientboundSetDefaultSpawnPositionPacket(BlockPos p_133115_, float p_133116_)
    {
        this.pos = p_133115_;
        this.angle = p_133116_;
    }

    public ClientboundSetDefaultSpawnPositionPacket(FriendlyByteBuf p_179286_)
    {
        this.pos = p_179286_.readBlockPos();
        this.angle = p_179286_.readFloat();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBlockPos(this.pos);
        pBuf.writeFloat(this.angle);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetSpawn(this);
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public float getAngle()
    {
        return this.angle;
    }
}
