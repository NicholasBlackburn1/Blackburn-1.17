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

public class OldChunkStorage {
   private static final int DATALAYER_BITS = 7;
   private static final LevelHeightAccessor OLD_LEVEL_HEIGHT = new LevelHeightAccessor() {
      public int getMinBuildHeight() {
         return 0;
      }

      public int getHeight() {
         return 128;
      }
   };

   public static OldChunkStorage.OldLevelChunk load(CompoundTag p_63592_) {
      int i = p_63592_.getInt("xPos");
      int j = p_63592_.getInt("zPos");
      OldChunkStorage.OldLevelChunk oldchunkstorage$oldlevelchunk = new OldChunkStorage.OldLevelChunk(i, j);
      oldchunkstorage$oldlevelchunk.blocks = p_63592_.getByteArray("Blocks");
      oldchunkstorage$oldlevelchunk.data = new OldDataLayer(p_63592_.getByteArray("Data"), 7);
      oldchunkstorage$oldlevelchunk.skyLight = new OldDataLayer(p_63592_.getByteArray("SkyLight"), 7);
      oldchunkstorage$oldlevelchunk.blockLight = new OldDataLayer(p_63592_.getByteArray("BlockLight"), 7);
      oldchunkstorage$oldlevelchunk.heightmap = p_63592_.getByteArray("HeightMap");
      oldchunkstorage$oldlevelchunk.terrainPopulated = p_63592_.getBoolean("TerrainPopulated");
      oldchunkstorage$oldlevelchunk.entities = p_63592_.getList("Entities", 10);
      oldchunkstorage$oldlevelchunk.blockEntities = p_63592_.getList("TileEntities", 10);
      oldchunkstorage$oldlevelchunk.blockTicks = p_63592_.getList("TileTicks", 10);

      try {
         oldchunkstorage$oldlevelchunk.lastUpdated = p_63592_.getLong("LastUpdate");
      } catch (ClassCastException classcastexception) {
         oldchunkstorage$oldlevelchunk.lastUpdated = (long)p_63592_.getInt("LastUpdate");
      }

      return oldchunkstorage$oldlevelchunk;
   }

   public static void convertToAnvilFormat(RegistryAccess.RegistryHolder p_63587_, OldChunkStorage.OldLevelChunk p_63588_, CompoundTag p_63589_, BiomeSource p_63590_) {
      p_63589_.putInt("xPos", p_63588_.x);
      p_63589_.putInt("zPos", p_63588_.z);
      p_63589_.putLong("LastUpdate", p_63588_.lastUpdated);
      int[] aint = new int[p_63588_.heightmap.length];

      for(int i = 0; i < p_63588_.heightmap.length; ++i) {
         aint[i] = p_63588_.heightmap[i];
      }

      p_63589_.putIntArray("HeightMap", aint);
      p_63589_.putBoolean("TerrainPopulated", p_63588_.terrainPopulated);
      ListTag listtag = new ListTag();

      for(int j = 0; j < 8; ++j) {
         boolean flag = true;

         for(int k = 0; k < 16 && flag; ++k) {
            for(int l = 0; l < 16 && flag; ++l) {
               for(int i1 = 0; i1 < 16; ++i1) {
                  int j1 = k << 11 | i1 << 7 | l + (j << 4);
                  int k1 = p_63588_.blocks[j1];
                  if (k1 != 0) {
                     flag = false;
                     break;
                  }
               }
            }
         }

         if (!flag) {
            byte[] abyte = new byte[4096];
            DataLayer datalayer = new DataLayer();
            DataLayer datalayer1 = new DataLayer();
            DataLayer datalayer2 = new DataLayer();

            for(int l2 = 0; l2 < 16; ++l2) {
               for(int l1 = 0; l1 < 16; ++l1) {
                  for(int i2 = 0; i2 < 16; ++i2) {
                     int j2 = l2 << 11 | i2 << 7 | l1 + (j << 4);
                     int k2 = p_63588_.blocks[j2];
                     abyte[l1 << 8 | i2 << 4 | l2] = (byte)(k2 & 255);
                     datalayer.set(l2, l1, i2, p_63588_.data.get(l2, l1 + (j << 4), i2));
                     datalayer1.set(l2, l1, i2, p_63588_.skyLight.get(l2, l1 + (j << 4), i2));
                     datalayer2.set(l2, l1, i2, p_63588_.blockLight.get(l2, l1 + (j << 4), i2));
                  }
               }
            }

            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putByte("Y", (byte)(j & 255));
            compoundtag.putByteArray("Blocks", abyte);
            compoundtag.putByteArray("Data", datalayer.getData());
            compoundtag.putByteArray("SkyLight", datalayer1.getData());
            compoundtag.putByteArray("BlockLight", datalayer2.getData());
            listtag.add(compoundtag);
         }
      }

      p_63589_.put("Sections", listtag);
      p_63589_.putIntArray("Biomes", (new ChunkBiomeContainer(p_63587_.registryOrThrow(Registry.BIOME_REGISTRY), OLD_LEVEL_HEIGHT, new ChunkPos(p_63588_.x, p_63588_.z), p_63590_)).writeBiomes());
      p_63589_.put("Entities", p_63588_.entities);
      p_63589_.put("TileEntities", p_63588_.blockEntities);
      if (p_63588_.blockTicks != null) {
         p_63589_.put("TileTicks", p_63588_.blockTicks);
      }

      p_63589_.putBoolean("convertedFromAlphaFormat", true);
   }

   public static class OldLevelChunk {
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

      public OldLevelChunk(int p_63606_, int p_63607_) {
         this.x = p_63606_;
         this.z = p_63607_;
      }
   }
}