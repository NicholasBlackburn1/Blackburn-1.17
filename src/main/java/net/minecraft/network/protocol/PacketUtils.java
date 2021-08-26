package net.minecraft.network.protocol;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import net.optifine.util.PacketRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketUtils
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static ResourceKey<Level> lastDimensionType = null;

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, ServerLevel pLevel) throws RunningOnDifferentThreadException
    {
        ensureRunningOnSameThread(pPacket, pProcessor, pLevel.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, BlockableEventLoop<?> pLevel) throws RunningOnDifferentThreadException
    {
        if (!pLevel.isSameThread())
        {
            pLevel.execute(new PacketRunnable(pPacket, () ->
            {
                clientPreProcessPacket(pPacket);

                if (pProcessor.getConnection().isConnected())
                {
                    pPacket.handle(pProcessor);
                }
                else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)pPacket);
                }
            }));
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
        else
        {
            clientPreProcessPacket(pPacket);
        }
    }

    protected static void clientPreProcessPacket(Packet packetIn)
    {
        if (packetIn instanceof ClientboundPlayerPositionPacket)
        {
            Minecraft.getInstance().levelRenderer.onPlayerPositionSet();
        }

        if (packetIn instanceof ClientboundRespawnPacket)
        {
            ClientboundRespawnPacket clientboundrespawnpacket = (ClientboundRespawnPacket)packetIn;
            lastDimensionType = clientboundrespawnpacket.getDimension();
        }
        else if (packetIn instanceof ClientboundLoginPacket)
        {
            ClientboundLoginPacket clientboundloginpacket = (ClientboundLoginPacket)packetIn;
            lastDimensionType = clientboundloginpacket.getDimension();
        }
        else
        {
            lastDimensionType = null;
        }
    }
}
