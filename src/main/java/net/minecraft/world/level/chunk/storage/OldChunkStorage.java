package net.minecraft.world.level.chunk.storage;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;

public class OldChunkStorage
{
    private static final int DATALAYER_BITS = 7;
    private static final LevelHeightAccessor OLD_LEVEL_HEIGHT = new LevelHeightAccessor()
    {
        public int getMinBuildHeight()
        {
            return 0;
        }
        public int getHeight()
        {
            return 128;
        }
    };

    public static OldChunkStorage.OldLevelChunk load(CompoundTag pChunkData)
    {
        int i = pChunkData.getInt("xPos");
        int j = pChunkData.getInt("zPos");
        OldChunkStorage.OldLevelChunk oldchunkstorage$oldlevelchunk = new OldChunkStorage.OldLevelChunk(i, j);
        oldchunkstorage$oldlevelchunk.blocks = pChunkData.getByteArray("Blocks");
        oldchunkstorage$oldlevelchunk.data = new OldDataLayer(pChunkData.getByteArray("Data"), 7);
        oldchunkstorage$oldlevelchunk.skyLight = new OldDataLayer(pChunkData.getByteArray("SkyLight"), 7);
        oldchunkstorage$oldlevelchunk.blockLight = new OldDataLayer(pChunkData.getByteArray("BlockLight"), 7);
        oldchunkstorage$oldlevelchunk.heightmap = pChunkData.getByteArray("HeightMap");
        oldchunkstorage$oldlevelchunk.terrainPopulated = pChunkData.getBoolean("TerrainPopulated");
        oldchunkstorage$oldlevelchunk.entities = pChunkData.getList("Entities", 10);
        oldchunkstorage$oldlevelchunk.blockEntities = pChunkData.getList("TileEntities", 10);
        oldchunkstorage$oldlevelchunk.blockTicks = pChunkData.getList("TileTicks", 10);

        try
        {
            oldchunkstorage$oldlevelchunk.lastUpdated = pChunkData.getLong("LastUpdate");
        }
        catch (ClassCastException classcastexception)
        {
            oldchunkstorage$oldlevelchunk.lastUpdated = (long)pChunkData.getInt("LastUpdate");
        }

        return oldchunkstorage$oldlevelchunk;
    }

    public static void convertToAnvilFormat(RegistryAccess.RegistryHolder pRegistryHolder, OldChunkStorage.OldLevelChunk pOldChunk, CompoundTag pNewChunkData, BiomeSource pBiomeSource)
    {
        pNewChunkData.putInt("xPos", pOldChunk.x);
        pNewChunkData.putInt("zPos", pOldChunk.z);
        pNewChunkData.putLong("LastUpdate", pOldChunk.lastUpdated);
        int[] aint = new int[pOldChunk.heightmap.length];

        for (int i = 0; i < pOldChunk.heightmap.length; ++i)
        {
            aint[i] = pOldChunk.heightmap[i];
        }

        pNewChunkData.m_128385_("HeightMap", aint);
        pNewChunkData.putBoolean("TerrainPopulated", pOldChunk.terrainPopulated);
        ListTag listtag = new ListTag();

        for (int j = 0; j < 8; ++j)
        {
            boolean flag = true;

            for (int k = 0; k < 16 && flag; ++k)
            {
                for (int l = 0; l < 16 && flag; ++l)
                {
                    for (int i1 = 0; i1 < 16; ++i1)
                    {
                        int j1 = k << 11 | i1 << 7 | l + (j << 4);
                        int k1 = pOldChunk.blocks[j1];

                        if (k1 != 0)
                        {
                            flag = false;
                            break;
                        }
                    }
                }
            }

            if (!flag)
            {
                byte[] abyte = new byte[4096];
                DataLayer datalayer = new DataLayer();
                DataLayer datalayer1 = new DataLayer();
                DataLayer datalayer2 = new DataLayer();

                for (int l2 = 0; l2 < 16; ++l2)
                {
                    for (int l1 = 0; l1 < 16; ++l1)
                    {
                        for (int i2 = 0; i2 < 16; ++i2)
                        {
                            int j2 = l2 << 11 | i2 << 7 | l1 + (j << 4);
                            int k2 = pOldChunk.blocks[j2];
                            abyte[l1 << 8 | i2 << 4 | l2] = (byte)(k2 & 255);
                            datalayer.set(l2, l1, i2, pOldChunk.data.get(l2, l1 + (j << 4), i2));
                            datalayer1.set(l2, l1, i2, pOldChunk.skyLight.get(l2, l1 + (j << 4), i2));
                            datalayer2.set(l2, l1, i2, pOldChunk.blockLight.get(l2, l1 + (j << 4), i2));
                        }
                    }
                }

                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Y", (byte)(j & 255));
                compoundtag.m_128382_("Blocks", abyte);
                compoundtag.m_128382_("Data", datalayer.getData());
                compoundtag.m_128382_("SkyLight", datalayer1.getData());
                compoundtag.m_128382_("BlockLight", datalayer2.getData());
                listtag.add(compoundtag);
            }
        }

        pNewChunkData.put("Sections", listtag);
        pNewChunkData.m_128385_("Biomes", (new ChunkBiomeContainer(pRegistryHolder.registryOrThrow(Registry.BIOME_REGISTRY), OLD_LEVEL_HEIGHT, new ChunkPos(pOldChunk.x, pOldChunk.z), pBiomeSource)).writeBiomes());
        pNewChunkData.put("Entities", pOldChunk.entities);
        pNewChunkData.put("TileEntities", pOldChunk.blockEntities);

        if (pOldChunk.blockTicks != null)
        {
            pNewChunkData.put("TileTicks", pOldChunk.blockTicks);
        }

        pNewChunkData.putBoolean("convertedFromAlphaFormat", true);
    }

    public static class OldLevelChunk
    {
        public long lastUpdated;
        public boolean terrainPopulated;
        public byte[] heightmap;
        public OldDataLayer blockLight;
        public OldDataLayer skyLight;
        public OldDataLayer data;
        public byte[] blocks;
        public ListTag entities;
        public ListTag blockEntities;
        public ListTag blockTicks;
        public final int x;
        public final int z;

        public OldLevelChunk(int pX, int pZ)
        {
            this.x = pX;
            this.z = pZ;
        }
    }
}
