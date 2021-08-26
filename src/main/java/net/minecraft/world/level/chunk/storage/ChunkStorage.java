package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable
{
    private final IOWorker worker;
    protected final DataFixer fixerUpper;
    @Nullable
    private LegacyStructureDataHandler legacyStructureHandler;

    public ChunkStorage(File pRegionFolder, DataFixer pFixerUpper, boolean pSync)
    {
        this.fixerUpper = pFixerUpper;
        this.worker = new IOWorker(pRegionFolder, pSync, "chunk");
    }

    public CompoundTag upgradeChunkTag(ResourceKey<Level> pLevelKey, Supplier<DimensionDataStorage> pStorage, CompoundTag pChunkData)
    {
        int i = getVersion(pChunkData);
        int j = 1493;

        if (i < 1493)
        {
            pChunkData = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, pChunkData, i, 1493);

            if (pChunkData.getCompound("Level").getBoolean("hasLegacyStructureData"))
            {
                if (this.legacyStructureHandler == null)
                {
                    this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(pLevelKey, pStorage.get());
                }

                pChunkData = this.legacyStructureHandler.updateFromLegacy(pChunkData);
            }
        }

        pChunkData = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, pChunkData, Math.max(1493, i));

        if (i < SharedConstants.getCurrentVersion().getWorldVersion())
        {
            pChunkData.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        }

        return pChunkData;
    }

    public static int getVersion(CompoundTag pChunkData)
    {
        return pChunkData.contains("DataVersion", 99) ? pChunkData.getInt("DataVersion") : -1;
    }

    @Nullable
    public CompoundTag read(ChunkPos pChunkPos) throws IOException
    {
        return this.worker.load(pChunkPos);
    }

    public void write(ChunkPos pChunkPos, CompoundTag pChunkData)
    {
        this.worker.store(pChunkPos, pChunkData);

        if (this.legacyStructureHandler != null)
        {
            this.legacyStructureHandler.removeIndex(pChunkPos.toLong());
        }
    }

    public void flushWorker()
    {
        this.worker.m_182498_(true).join();
    }

    public void close() throws IOException
    {
        this.worker.close();
    }
}
