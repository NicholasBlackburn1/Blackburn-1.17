package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

public interface ServerLoginPacketListener extends PacketListener
{
    void handleHello(ServerboundHelloPacket pPacket);

    void handleKey(ServerboundKeyPacket pPacket);

    void handleCustomQueryPacket(ServerboundCustomQueryPacket p_134822_);
}
