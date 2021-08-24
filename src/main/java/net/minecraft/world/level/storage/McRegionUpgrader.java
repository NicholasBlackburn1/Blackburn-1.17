package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String MCREGION_EXTENSION = ".mcr";

   static boolean convertLevel(LevelStorageSource.LevelStorageAccess p_78401_, ProgressListener p_78402_) {
      p_78402_.progressStagePercentage(0);
      List<File> list = Lists.newArrayList();
      List<File> list1 = Lists.newArrayList();
      List<File> list2 = Lists.newArrayList();
      File file1 = p_78401_.getDimensionPath(Level.OVERWORLD);
      File file2 = p_78401_.getDimensionPath(Level.NETHER);
      File file3 = p_78401_.getDimensionPath(Level.END);
      LOGGER.info("Scanning folders...");
      addRegionFiles(file1, list);
      if (file2.exists()) {
         addRegionFiles(file2, list1);
      }

      if (file3.exists()) {
         addRegionFiles(file3, list2);
      }

      int i = list.size() + list1.size() + list2.size();
      LOGGER.info("Total conversion count is {}", (int)i);
      RegistryAccess.RegistryHolder registryaccess$registryholder = RegistryAccess.builtin();
      RegistryReadOps<Tag> registryreadops = RegistryReadOps.createAndLoad(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, registryaccess$registryholder);
      WorldData worlddata = p_78401_.getDataTag(registryreadops, DataPackConfig.DEFAULT);
      long j = worlddata != null ? worlddata.worldGenSettings().seed() : 0L;
      Registry<Biome> registry = registryaccess$registryholder.registryOrThrow(Registry.BIOME_REGISTRY);
      BiomeSource biomesource;
      if (worlddata != null && worlddata.worldGenSettings().isFlatWorld()) {
         biomesource = new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS));
      } else {
         biomesource = new OverworldBiomeSource(j, false, false, registry);
      }

      convertRegions(registryaccess$registryholder, new File(file1, "region"), list, biomesource, 0, i, p_78402_);
      convertRegions(registryaccess$registryholder, new File(file2, "region"), list1, new FixedBiomeSource(registry.getOrThrow(Biomes.NETHER_WASTES)), list.size(), i, p_78402_);
      convertRegions(registryaccess$registryholder, new File(file3, "region"), list2, new FixedBiomeSource(registry.getOrThrow(Biomes.THE_END)), list.size() + list1.size(), i, p_78402_);
      makeMcrLevelDatBackup(p_78401_);
      p_78401_.saveDataTag(registryaccess$registryholder, worlddata);
      return true;
   }

   private static void makeMcrLevelDatBackup(LevelStorageSource.LevelStorageAccess p_78399_) {
      File file1 = p_78399_.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
      if (!file1.exists()) {
         LOGGER.warn("Unable to create level.dat_mcr backup");
      } else {
         File file2 = new File(file1.getParent(), "level.dat_mcr");
         if (!file1.renameTo(file2)) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
         }

      }
   }

   private static void convertRegions(RegistryAccess.RegistryHolder p_78412_, File p_78413_, Iterable<File> p_78414_, BiomeSource p_78415_, int p_78416_, int p_78417_, ProgressListener p_78418_) {
      for(File file1 : p_78414_) {
         convertRegion(p_78412_, p_78413_, file1, p_78415_, p_78416_, p_78417_, p_78418_);
         ++p_78416_;
         int i = (int)Math.round(100.0D * (double)p_78416_ / (double)p_78417_);
         p_78418_.progressStagePercentage(i);
      }

   }

   private static void convertRegion(RegistryAccess.RegistryHolder p_78404_, File p_78405_, File p_78406_, BiomeSource p_78407_, int p_78408_, int p_78409_, ProgressListener p_78410_) {
      String s = p_78406_.getName();

      try {
         RegionFile regionfile = new RegionFile(p_78406_, p_78405_, true);

         try {
            RegionFile regionfile1 = new RegionFile(new File(p_78405_, s.substring(0, s.length() - ".mcr".length()) + ".mca"), p_78405_, true);

            try {
               for(int i = 0; i < 32; ++i) {
                  for(int j = 0; j < 32; ++j) {
                     ChunkPos chunkpos = new ChunkPos(i, j);
                     if (regionfile.hasChunk(chunkpos) && !regionfile1.hasChunk(chunkpos)) {
                        CompoundTag compoundtag;
                        try {
                           DataInputStream datainputstream = regionfile.getChunkDataInputStream(chunkpos);

                           label111: {
                              try {
                                 if (datainputstream != null) {
                                    compoundtag = NbtIo.read(datainputstream);
                                    break label111;
                                 }

                                 LOGGER.warn("Failed to fetch input stream for chunk {}", (Object)chunkpos);
                              } catch (Throwable throwable5) {
                                 if (datainputstream != null) {
                                    try {
                                       datainputstream.close();
                                    } catch (Throwable throwable3) {
                                       throwable5.addSuppressed(throwable3);
                                    }
                                 }

                                 throw throwable5;
                              }

                              if (datainputstream != null) {
                                 datainputstream.close();
                              }
                              continue;
                           }

                           if (datainputstream != null) {
                              datainputstream.close();
                           }
                        } catch (IOException ioexception) {
                           LOGGER.warn("Failed to read data for chunk {}", chunkpos, ioexception);
                           continue;
                        }

                        CompoundTag compoundtag3 = compoundtag.getCompound("Level");
                        OldChunkStorage.OldLevelChunk oldchunkstorage$oldlevelchunk = OldChunkStorage.load(compoundtag3);
                        CompoundTag compoundtag1 = new CompoundTag();
                        CompoundTag compoundtag2 = new CompoundTag();
                        compoundtag1.put("Level", compoundtag2);
                        OldChunkStorage.convertToAnvilFormat(p_78404_, oldchunkstorage$oldlevelchunk, compoundtag2, p_78407_);
                        DataOutputStream dataoutputstream = regionfile1.getChunkDataOutputStream(chunkpos);

                        try {
                           NbtIo.write(compoundtag1, dataoutputstream);
                        } catch (Throwable throwable4) {
                           if (dataoutputstream != null) {
                              try {
                                 dataoutputstream.close();
                              } catch (Throwable throwable2) {
                                 throwable4.addSuppressed(throwable2);
                              }
                           }

                           throw throwable4;
                        }

                        if (dataoutputstream != null) {
                           dataoutputstream.close();
                        }
                     }
                  }

                  int k = (int)Math.round(100.0D * (double)(p_78408_ * 1024) / (double)(p_78409_ * 1024));
                  int l = (int)Math.round(100.0D * (double)((i + 1) * 32 + p_78408_ * 1024) / (double)(p_78409_ * 1024));
                  if (l > k) {
                     p_78410_.progressStagePercentage(l);
                  }
               }
            } catch (Throwable throwable6) {
               try {
                  regionfile1.close();
               } catch (Throwable throwable1) {
                  throwable6.addSuppressed(throwable1);
               }

               throw throwable6;
            }

            regionfile1.close();
         } catch (Throwable throwable7) {
            try {
               regionfile.close();
            } catch (Throwable throwable) {
               throwable7.addSuppressed(throwable);
            }

            throw throwable7;
         }

         regionfile.close();
      } catch (IOException ioexception1) {
         LOGGER.error("Failed to upgrade region file {}", p_78406_, ioexception1);
      }

   }

   private static void addRegionFiles(File p_78423_, Collection<File> p_78424_) {
      File file1 = new File(p_78423_, "region");
      File[] afile = file1.listFiles((p_78420_, p_78421_) -> {
         return p_78421_.endsWith(".mcr");
      });
      if (afile != null) {
         Collections.addAll(p_78424_, afile);
      }

   }
}