package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener>
{
    private final InteractionHand hand;

    public ClientboundOpenBookPacket(InteractionHand p_132601_)
    {
        this.hand = p_132601_;
    }

    public ClientboundOpenBookPacket(FriendlyByteBuf p_179009_)
    {
        this.hand = p_179009_.readEnum(InteractionHand.class);
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeEnum(this.hand);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleOpenBook(this);
    }

    public InteractionHand getHand()
    {
        return this.hand;
    }
}
