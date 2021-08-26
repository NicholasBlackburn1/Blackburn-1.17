package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerList
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final List<ServerData> serverList = Lists.newArrayList();

    public ServerList(Minecraft p_105430_)
    {
        this.minecraft = p_105430_;
        this.load();
    }

    public void load()
    {
        try
        {
            this.serverList.clear();
            CompoundTag compoundtag = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));

            if (compoundtag == null)
            {
                return;
            }

            ListTag listtag = compoundtag.getList("servers", 10);

            for (int i = 0; i < listtag.size(); ++i)
            {
                this.serverList.add(ServerData.read(listtag.getCompound(i)));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't load server list", (Throwable)exception);
        }
    }

    public void save()
    {
        try
        {
            ListTag listtag = new ListTag();

            for (ServerData serverdata : this.serverList)
            {
                listtag.add(serverdata.write());
            }

            CompoundTag compoundtag = new CompoundTag();
            compoundtag.put("servers", listtag);
            File file3 = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
            NbtIo.write(compoundtag, file3);
            File file1 = new File(this.minecraft.gameDirectory, "servers.dat_old");
            File file2 = new File(this.minecraft.gameDirectory, "servers.dat");
            Util.safeReplaceFile(file2, file3, file1);
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't save server list", (Throwable)exception);
        }
    }

    public ServerData get(int pIndex)
    {
        return this.serverList.get(pIndex);
    }

    public void remove(ServerData pServerData)
    {
        this.serverList.remove(pServerData);
    }

    public void add(ServerData pServer)
    {
        this.serverList.add(pServer);
    }

    public int size()
    {
        return this.serverList.size();
    }

    public void swap(int pPos1, int pPos2)
    {
        ServerData serverdata = this.get(pPos1);
        this.serverList.set(pPos1, this.get(pPos2));
        this.serverList.set(pPos2, serverdata);
        this.save();
    }

    public void replace(int pIndex, ServerData pServer)
    {
        this.serverList.set(pIndex, pServer);
    }

    public static void saveSingleServer(ServerData pServer)
    {
        ServerList serverlist = new ServerList(Minecraft.getInstance());
        serverlist.load();

        for (int i = 0; i < serverlist.size(); ++i)
        {
            ServerData serverdata = serverlist.get(i);

            if (serverdata.name.equals(pServer.name) && serverdata.ip.equals(pServer.ip))
            {
                serverlist.replace(i, pServer);
                break;
            }
        }

        serverlist.save();
    }
}
