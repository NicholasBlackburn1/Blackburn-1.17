package net.minecraft.client.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LanServerDetection
{
    static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogManager.getLogger();

    public static class LanServerDetector extends Thread
    {
        private final LanServerDetection.LanServerList serverList;
        private final InetAddress pingGroup;
        private final MulticastSocket socket;

        public LanServerDetector(LanServerDetection.LanServerList p_120090_) throws IOException
        {
            super("LanServerDetector #" + LanServerDetection.UNIQUE_THREAD_ID.incrementAndGet());
            this.serverList = p_120090_;
            this.setDaemon(true);
            this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LanServerDetection.LOGGER));
            this.socket = new MulticastSocket(4445);
            this.pingGroup = InetAddress.getByName("224.0.2.60");
            this.socket.setSoTimeout(5000);
            this.socket.joinGroup(this.pingGroup);
        }

        public void run()
        {
            byte[] abyte = new byte[1024];

            while (!this.isInterrupted())
            {
                DatagramPacket datagrampacket = new DatagramPacket(abyte, abyte.length);

                try
                {
                    this.socket.receive(datagrampacket);
                }
                catch (SocketTimeoutException sockettimeoutexception)
                {
                    continue;
                }
                catch (IOException ioexception1)
                {
                    LanServerDetection.LOGGER.error("Couldn't ping server", (Throwable)ioexception1);
                    break;
                }

                String s = new String(datagrampacket.getData(), datagrampacket.getOffset(), datagrampacket.getLength(), StandardCharsets.UTF_8);
                LanServerDetection.LOGGER.debug("{}: {}", datagrampacket.getAddress(), s);
                this.serverList.addServer(s, datagrampacket.getAddress());
            }

            try
            {
                this.socket.leaveGroup(this.pingGroup);
            }
            catch (IOException ioexception)
            {
            }

            this.socket.close();
        }
    }

    public static class LanServerList
    {
        private final List<LanServer> servers = Lists.newArrayList();
        private boolean isDirty;

        public synchronized boolean isDirty()
        {
            return this.isDirty;
        }

        public synchronized void markClean()
        {
            this.isDirty = false;
        }

        public synchronized List<LanServer> getServers()
        {
            return Collections.unmodifiableList(this.servers);
        }

        public synchronized void addServer(String pPingResponse, InetAddress pIpAddress)
        {
            String s = LanServerPinger.parseMotd(pPingResponse);
            String s1 = LanServerPinger.parseAddress(pPingResponse);

            if (s1 != null)
            {
                s1 = pIpAddress.getHostAddress() + ":" + s1;
                boolean flag = false;

                for (LanServer lanserver : this.servers)
                {
                    if (lanserver.getAddress().equals(s1))
                    {
                        lanserver.updatePingTime();
                        flag = true;
                        break;
                    }
                }

                if (!flag)
                {
                    this.servers.add(new LanServer(s, s1));
                    this.isDirty = true;
                }
            }
        }
    }
}
