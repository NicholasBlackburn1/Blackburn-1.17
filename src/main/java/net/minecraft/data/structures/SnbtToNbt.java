package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider
{
    @Nullable
    private static final Path DUMP_SNBT_TO = null;
    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;
    private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

    public SnbtToNbt(DataGenerator p_126448_)
    {
        this.generator = p_126448_;
    }

    public SnbtToNbt addFilter(SnbtToNbt.Filter pTransformer)
    {
        this.filters.add(pTransformer);
        return this;
    }

    private CompoundTag applyFilters(String pFileName, CompoundTag pNbt)
    {
        CompoundTag compoundtag = pNbt;

        for (SnbtToNbt.Filter snbttonbt$filter : this.filters)
        {
            compoundtag = snbttonbt$filter.apply(pFileName, compoundtag);
        }

        return compoundtag;
    }

    public void run(HashCache pCache) throws IOException
    {
        Path path = this.generator.getOutputFolder();
        List<CompletableFuture<SnbtToNbt.TaskResult>> list = Lists.newArrayList();

        for (Path path1 : this.generator.getInputFolders())
        {
            Files.walk(path1).filter((p_126464_) ->
            {
                return p_126464_.toString().endsWith(".snbt");
            }).forEach((p_126474_) ->
            {
                list.add(CompletableFuture.supplyAsync(() -> {
                    return this.readStructure(p_126474_, this.getName(path1, p_126474_));
                }, Util.backgroundExecutor()));
            });
        }

        boolean flag = false;

        for (CompletableFuture<SnbtToNbt.TaskResult> completablefuture : list)
        {
            try
            {
                this.storeStructureIfChanged(pCache, completablefuture.get(), path);
            }
            catch (Exception exception)
            {
                LOGGER.error("Failed to process structure", (Throwable)exception);
                flag = true;
            }
        }

        if (flag)
        {
            throw new IllegalStateException("Failed to convert all structures, aborting");
        }
    }

    public String getName()
    {
        return "SNBT -> NBT";
    }

    private String getName(Path p_126469_, Path p_126470_)
    {
        String s = p_126469_.relativize(p_126470_).toString().replaceAll("\\\\", "/");
        return s.substring(0, s.length() - ".snbt".length());
    }

    private SnbtToNbt.TaskResult readStructure(Path pFilePath, String pFileName)
    {
        try
        {
            BufferedReader bufferedreader = Files.newBufferedReader(pFilePath);
            SnbtToNbt.TaskResult snbttonbt$taskresult;

            try
            {
                String s = IOUtils.toString((Reader)bufferedreader);
                CompoundTag compoundtag = this.applyFilters(pFileName, NbtUtils.snbtToStructure(s));
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                NbtIo.writeCompressed(compoundtag, bytearrayoutputstream);
                byte[] abyte = bytearrayoutputstream.toByteArray();
                String s1 = SHA1.hashBytes(abyte).toString();
                String s2;

                if (DUMP_SNBT_TO != null)
                {
                    s2 = NbtUtils.structureToSnbt(compoundtag);
                }
                else
                {
                    s2 = null;
                }

                snbttonbt$taskresult = new SnbtToNbt.TaskResult(pFileName, abyte, s2, s1);
            }
            catch (Throwable throwable1)
            {
                if (bufferedreader != null)
                {
                    try
                    {
                        bufferedreader.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (bufferedreader != null)
            {
                bufferedreader.close();
            }

            return snbttonbt$taskresult;
        }
        catch (Throwable throwable2)
        {
            throw new SnbtToNbt.StructureConversionException(pFilePath, throwable2);
        }
    }

    private void storeStructureIfChanged(HashCache pDirectory, SnbtToNbt.TaskResult pTaskResult, Path pPath)
    {
        if (pTaskResult.snbtPayload != null)
        {
            Path path = DUMP_SNBT_TO.resolve(pTaskResult.name + ".snbt");

            try
            {
                NbtToSnbt.writeSnbt(path, pTaskResult.snbtPayload);
            }
            catch (IOException ioexception)
            {
                LOGGER.error("Couldn't write structure SNBT {} at {}", pTaskResult.name, path, ioexception);
            }
        }

        Path path1 = pPath.resolve(pTaskResult.name + ".nbt");

        try
        {
            if (!Objects.equals(pDirectory.getHash(path1), pTaskResult.hash) || !Files.exists(path1))
            {
                Files.createDirectories(path1.getParent());
                OutputStream outputstream = Files.newOutputStream(path1);

                try
                {
                    outputstream.write(pTaskResult.payload);
                }
                catch (Throwable throwable1)
                {
                    if (outputstream != null)
                    {
                        try
                        {
                            outputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (outputstream != null)
                {
                    outputstream.close();
                }
            }

            pDirectory.putNew(path1, pTaskResult.hash);
        }
        catch (IOException ioexception1)
        {
            LOGGER.error("Couldn't write structure {} at {}", pTaskResult.name, path1, ioexception1);
        }
    }

    @FunctionalInterface
    public interface Filter
    {
        CompoundTag apply(String p_126480_, CompoundTag p_126481_);
    }

    static class StructureConversionException extends RuntimeException
    {
        public StructureConversionException(Path p_176820_, Throwable p_176821_)
        {
            super(p_176820_.toAbsolutePath().toString(), p_176821_);
        }
    }

    static class TaskResult
    {
        final String name;
        final byte[] payload;
        @Nullable
        final String snbtPayload;
        final String hash;

        public TaskResult(String p_126487_, byte[] p_126488_, @Nullable String p_126489_, String p_126490_)
        {
            this.name = p_126487_;
            this.payload = p_126488_;
            this.snbtPayload = p_126489_;
            this.hash = p_126490_;
        }
    }
}
