package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener>
{
    private final Component reason;

    public ClientboundLoginDisconnectPacket(Component p_134812_)
    {
        this.reason = p_134812_;
    }

    public ClientboundLoginDisconnectPacket(FriendlyByteBuf p_179820_)
    {
        this.reason = Component.Serializer.fromJsonLenient(p_179820_.readUtf(262144));
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeComponent(this.reason);
    }

    public void handle(ClientLoginPacketListener pHandler)
    {
        pHandler.handleDisconnect(this);
    }

    public Component getReason()
    {
        return this.reason;
    }
}
