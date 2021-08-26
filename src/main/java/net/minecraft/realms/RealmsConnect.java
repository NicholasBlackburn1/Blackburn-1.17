package net.minecraft.realms;

import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsConnect
{
    static final Logger LOGGER = LogManager.getLogger();
    final Screen onlineScreen;
    volatile boolean aborted;
    Connection connection;

    public RealmsConnect(Screen pOnlineScreen)
    {
        this.onlineScreen = pOnlineScreen;
    }

    public void connect(final RealmsServer pServer, ServerAddress pAddress)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.setConnectedToRealms(true);
        NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("mco.connect.success"));
        final String s = pAddress.getHost();
        final int i = pAddress.getPort();
        (new Thread("Realms-connect-task")
        {
            public void run()
            {
                InetSocketAddress inetsocketaddress = null;

                try
                {
                    inetsocketaddress = new InetSocketAddress(s, i);

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection = Connection.connectToServer(inetsocketaddress, minecraft.options.useNativeTransport());

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.setListener(new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, RealmsConnect.this.onlineScreen, (p_120726_) ->
                    {
                    }));

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.send(new ClientIntentionPacket(s, i, ConnectionProtocol.LOGIN));

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
                    minecraft.setCurrentServer(pServer.toServerData(s));
                }
                catch (Exception exception)
                {
                    minecraft.getClientPackSource().clearServerPack();

                    if (RealmsConnect.this.aborted)
                    {
                        return;
                    }

                    RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)exception);
                    String s1 = exception.toString();

                    if (inetsocketaddress != null)
                    {
                        String s2 = inetsocketaddress + ":" + i;
                        s1 = s1.replaceAll(s2, "");
                    }

                    DisconnectedRealmsScreen disconnectedrealmsscreen = new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", s1));
                    minecraft.execute(() ->
                    {
                        minecraft.setScreen(disconnectedrealmsscreen);
                    });
                }
            }
        }).start();
    }

    public void abort()
    {
        this.aborted = true;

        if (this.connection != null && this.connection.isConnected())
        {
            this.connection.disconnect(new TranslatableComponent("disconnect.genericReason"));
            this.connection.handleDisconnection();
        }
    }

    public void tick()
    {
        if (this.connection != null)
        {
            if (this.connection.isConnected())
            {
                this.connection.tick();
            }
            else
            {
                this.connection.handleDisconnection();
            }
        }
    }
}
