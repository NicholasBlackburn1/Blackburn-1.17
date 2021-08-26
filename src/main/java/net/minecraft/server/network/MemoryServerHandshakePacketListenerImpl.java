package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener
{
    private final MinecraftServer server;
    private final Connection connection;

    public MemoryServerHandshakePacketListenerImpl(MinecraftServer p_9691_, Connection p_9692_)
    {
        this.server = p_9691_;
        this.connection = p_9692_;
    }

    public void handleIntention(ClientIntentionPacket pPacket)
    {
        this.connection.setProtocol(pPacket.getIntention());
        this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
    }

    public void onDisconnect(Component pReason)
    {
    }

    public Connection getConnection()
    {
        return this.connection;
    }
}
