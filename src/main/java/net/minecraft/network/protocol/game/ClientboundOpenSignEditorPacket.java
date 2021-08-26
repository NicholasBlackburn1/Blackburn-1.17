package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener>
{
    private final BlockPos pos;

    public ClientboundOpenSignEditorPacket(BlockPos p_132633_)
    {
        this.pos = p_132633_;
    }

    public ClientboundOpenSignEditorPacket(FriendlyByteBuf p_179013_)
    {
        this.pos = p_179013_.readBlockPos();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBlockPos(this.pos);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleOpenSignEditor(this);
    }

    public BlockPos getPos()
    {
        return this.pos;
    }
}
