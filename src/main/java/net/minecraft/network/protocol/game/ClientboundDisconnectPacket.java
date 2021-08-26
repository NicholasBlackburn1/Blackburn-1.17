package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientGamePacketListener>
{
    private final Component reason;

    public ClientboundDisconnectPacket(Component p_132078_)
    {
        this.reason = p_132078_;
    }

    public ClientboundDisconnectPacket(FriendlyByteBuf p_178841_)
    {
        this.reason = p_178841_.readComponent();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeComponent(this.reason);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleDisconnect(this);
    }

    public Component getReason()
    {
        return this.reason;
    }
}
