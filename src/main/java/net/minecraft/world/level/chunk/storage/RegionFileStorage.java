package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable
{
    public static final String ANVIL_EXTENSION = ".mca";
    private static final int MAX_CACHE_SIZE = 256;
    private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
    private final File folder;
    private final boolean sync;

    RegionFileStorage(File pFolder, boolean pSync)
    {
        this.folder = pFolder;
        this.sync = pSync;
    }

    private RegionFile getRegionFile(ChunkPos pChunkPos) throws IOException
    {
        long i = ChunkPos.asLong(pChunkPos.getRegionX(), pChunkPos.getRegionZ());
        RegionFile regionfile = this.regionCache.getAndMoveToFirst(i);

        if (regionfile != null)
        {
            return regionfile;
        }
        else
        {
            if (this.regionCache.size() >= 256)
            {
                this.regionCache.removeLast().close();
            }

            if (!this.folder.exists())
            {
                this.folder.mkdirs();
            }

            File file1 = new File(this.folder, "r." + pChunkPos.getRegionX() + "." + pChunkPos.getRegionZ() + ".mca");
            RegionFile regionfile1 = new RegionFile(file1, this.folder, this.sync);
            this.regionCache.putAndMoveToFirst(i, regionfile1);
            return regionfile1;
        }
    }

    @Nullable
    public CompoundTag read(ChunkPos pChunkPos) throws IOException
    {
        RegionFile regionfile = this.getRegionFile(pChunkPos);
        DataInputStream datainputstream = regionfile.getChunkDataInputStream(pChunkPos);
        CompoundTag compoundtag;
        label43:
        {
            try
            {
                if (datainputstream == null)
                {
                    compoundtag = null;
                    break label43;
                }

                compoundtag = NbtIo.read(datainputstream);
            }
            catch (Throwable throwable1)
            {
                if (datainputstream != null)
                {
                    try
                    {
                        datainputstream.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (datainputstream != null)
            {
                datainputstream.close();
            }

            return compoundtag;
        }

        if (datainputstream != null)
        {
            datainputstream.close();
        }

        return compoundtag;
    }

    protected void write(ChunkPos pChunkPos, @Nullable CompoundTag pChunkData) throws IOException
    {
        RegionFile regionfile = this.getRegionFile(pChunkPos);

        if (pChunkData == null)
        {
            regionfile.clear(pChunkPos);
        }
        else
        {
            DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(pChunkPos);

            try
            {
                NbtIo.write(pChunkData, dataoutputstream);
            }
            catch (Throwable throwable1)
            {
                if (dataoutputstream != null)
                {
                    try
                    {
                        dataoutputstream.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (dataoutputstream != null)
            {
                dataoutputstream.close();
            }
        }
    }

    public void close() throws IOException
    {
        ExceptionCollector<IOException> exceptioncollector = new ExceptionCollector<>();

        for (RegionFile regionfile : this.regionCache.values())
        {
            try
            {
                regionfile.close();
            }
            catch (IOException ioexception)
            {
                exceptioncollector.add(ioexception);
            }
        }

        exceptioncollector.throwIfPresent();
    }

    public void flush() throws IOException
    {
        for (RegionFile regionfile : this.regionCache.values())
        {
            regionfile.flush();
        }
    }
}
