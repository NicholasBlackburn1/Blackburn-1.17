package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener>
{
    private final int id;
    private final BlockPos pos;
    private final int progress;

    public ClientboundBlockDestructionPacket(int p_131676_, BlockPos p_131677_, int p_131678_)
    {
        this.id = p_131676_;
        this.pos = p_131677_;
        this.progress = p_131678_;
    }

    public ClientboundBlockDestructionPacket(FriendlyByteBuf p_178606_)
    {
        this.id = p_178606_.readVarInt();
        this.pos = p_178606_.readBlockPos();
        this.progress = p_178606_.readUnsignedByte();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.id);
        pBuf.writeBlockPos(this.pos);
        pBuf.writeByte(this.progress);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleBlockDestruction(this);
    }

    public int getId()
    {
        return this.id;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public int getProgress()
    {
        return this.progress;
    }
}
