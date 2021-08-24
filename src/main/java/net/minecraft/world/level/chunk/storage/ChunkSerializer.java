package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final String TAG_UPGRADE_DATA = "UpgradeData";

   public static ProtoChunk read(ServerLevel p_63458_, StructureManager p_63459_, PoiManager p_63460_, ChunkPos p_63461_, CompoundTag p_63462_) {
      ChunkGenerator chunkgenerator = p_63458_.getChunkSource().getGenerator();
      BiomeSource biomesource = chunkgenerator.getBiomeSource();
      CompoundTag compoundtag = p_63462_.getCompound("Level");
      ChunkPos chunkpos = new ChunkPos(compoundtag.getInt("xPos"), compoundtag.getInt("zPos"));
      if (!Objects.equals(p_63461_, chunkpos)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", p_63461_, p_63461_, chunkpos);
      }

      ChunkBiomeContainer chunkbiomecontainer = new ChunkBiomeContainer(p_63458_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), p_63458_, p_63461_, biomesource, compoundtag.contains("Biomes", 11) ? compoundtag.getIntArray("Biomes") : null);
      UpgradeData upgradedata = compoundtag.contains("UpgradeData", 10) ? new UpgradeData(compoundtag.getCompound("UpgradeData"), p_63458_) : UpgradeData.EMPTY;
      ProtoTickList<Block> prototicklist = new ProtoTickList<>((p_63475_) -> {
         return p_63475_ == null || p_63475_.defaultBlockState().isAir();
      }, p_63461_, compoundtag.getList("ToBeTicked", 9), p_63458_);
      ProtoTickList<Fluid> prototicklist1 = new ProtoTickList<>((p_63484_) -> {
         return p_63484_ == null || p_63484_ == Fluids.EMPTY;
      }, p_63461_, compoundtag.getList("LiquidsToBeTicked", 9), p_63458_);
      boolean flag = compoundtag.getBoolean("isLightOn");
      ListTag listtag = compoundtag.getList("Sections", 10);
      int i = p_63458_.getSectionsCount();
      LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
      boolean flag1 = p_63458_.dimensionType().hasSkyLight();
      ChunkSource chunksource = p_63458_.getChunkSource();
      LevelLightEngine levellightengine = chunksource.getLightEngine();
      if (flag) {
         levellightengine.retainData(p_63461_, true);
      }

      for(int j = 0; j < listtag.size(); ++j) {
         CompoundTag compoundtag1 = listtag.getCompound(j);
         int k = compoundtag1.getByte("Y");
         if (compoundtag1.contains("Palette", 9) && compoundtag1.contains("BlockStates", 12)) {
            LevelChunkSection levelchunksection = new LevelChunkSection(k);
            levelchunksection.getStates().read(compoundtag1.getList("Palette", 10), compoundtag1.getLongArray("BlockStates"));
            levelchunksection.recalcBlockCounts();
            if (!levelchunksection.isEmpty()) {
               alevelchunksection[p_63458_.getSectionIndexFromSectionY(k)] = levelchunksection;
            }

            p_63460_.checkConsistencyWithBlocks(p_63461_, levelchunksection);
         }

         if (flag) {
            if (compoundtag1.contains("BlockLight", 7)) {
               levellightengine.queueSectionData(LightLayer.BLOCK, SectionPos.of(p_63461_, k), new DataLayer(compoundtag1.getByteArray("BlockLight")), true);
            }

            if (flag1 && compoundtag1.contains("SkyLight", 7)) {
               levellightengine.queueSectionData(LightLayer.SKY, SectionPos.of(p_63461_, k), new DataLayer(compoundtag1.getByteArray("SkyLight")), true);
            }
         }
      }

      long k1 = compoundtag.getLong("InhabitedTime");
      ChunkStatus.ChunkType chunkstatus$chunktype = getChunkTypeFromTag(p_63462_);
      ChunkAccess chunkaccess;
      if (chunkstatus$chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         TickList<Block> ticklist;
         if (compoundtag.contains("TileTicks", 9)) {
            ticklist = ChunkTickList.create(compoundtag.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
         } else {
            ticklist = prototicklist;
         }

         TickList<Fluid> ticklist1;
         if (compoundtag.contains("LiquidTicks", 9)) {
            ticklist1 = ChunkTickList.create(compoundtag.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
         } else {
            ticklist1 = prototicklist1;
         }

         chunkaccess = new LevelChunk(p_63458_.getLevel(), p_63461_, chunkbiomecontainer, upgradedata, ticklist, ticklist1, k1, alevelchunksection, (p_156533_) -> {
            postLoadChunk(p_63458_, compoundtag, p_156533_);
         });
      } else {
         ProtoChunk protochunk = new ProtoChunk(p_63461_, upgradedata, alevelchunksection, prototicklist, prototicklist1, p_63458_);
         protochunk.setBiomes(chunkbiomecontainer);
         chunkaccess = protochunk;
         protochunk.setInhabitedTime(k1);
         protochunk.setStatus(ChunkStatus.byName(compoundtag.getString("Status")));
         if (protochunk.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
            protochunk.setLightEngine(levellightengine);
         }

         if (!flag && protochunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
            for(BlockPos blockpos : BlockPos.betweenClosed(p_63461_.getMinBlockX(), p_63458_.getMinBuildHeight(), p_63461_.getMinBlockZ(), p_63461_.getMaxBlockX(), p_63458_.getMaxBuildHeight() - 1, p_63461_.getMaxBlockZ())) {
               if (chunkaccess.getBlockState(blockpos).getLightEmission() != 0) {
                  protochunk.addLight(blockpos);
               }
            }
         }
      }

      chunkaccess.setLightCorrect(flag);
      CompoundTag compoundtag3 = compoundtag.getCompound("Heightmaps");
      EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

      for(Heightmap.Types heightmap$types : chunkaccess.getStatus().heightmapsAfter()) {
         String s = heightmap$types.getSerializationKey();
         if (compoundtag3.contains(s, 12)) {
            chunkaccess.setHeightmap(heightmap$types, compoundtag3.getLongArray(s));
         } else {
            enumset.add(heightmap$types);
         }
      }

      Heightmap.primeHeightmaps(chunkaccess, enumset);
      CompoundTag compoundtag4 = compoundtag.getCompound("Structures");
      chunkaccess.setAllStarts(unpackStructureStart(p_63458_, compoundtag4, p_63458_.getSeed()));
      chunkaccess.setAllReferences(unpackStructureReferences(p_63461_, compoundtag4));
      if (compoundtag.getBoolean("shouldSave")) {
         chunkaccess.setUnsaved(true);
      }

      ListTag listtag3 = compoundtag.getList("PostProcessing", 9);

      for(int l1 = 0; l1 < listtag3.size(); ++l1) {
         ListTag listtag1 = listtag3.getList(l1);

         for(int l = 0; l < listtag1.size(); ++l) {
            chunkaccess.addPackedPostProcess(listtag1.getShort(l), l1);
         }
      }

      if (chunkstatus$chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         return new ImposterProtoChunk((LevelChunk)chunkaccess);
      } else {
         ProtoChunk protochunk1 = (ProtoChunk)chunkaccess;
         ListTag listtag4 = compoundtag.getList("Entities", 10);

         for(int i2 = 0; i2 < listtag4.size(); ++i2) {
            protochunk1.addEntity(listtag4.getCompound(i2));
         }

         ListTag listtag5 = compoundtag.getList("TileEntities", 10);

         for(int i1 = 0; i1 < listtag5.size(); ++i1) {
            CompoundTag compoundtag2 = listtag5.getCompound(i1);
            chunkaccess.setBlockEntityNbt(compoundtag2);
         }

         ListTag listtag6 = compoundtag.getList("Lights", 9);

         for(int j2 = 0; j2 < listtag6.size(); ++j2) {
            ListTag listtag2 = listtag6.getList(j2);

            for(int j1 = 0; j1 < listtag2.size(); ++j1) {
               protochunk1.addLight(listtag2.getShort(j1), j2);
            }
         }

         CompoundTag compoundtag5 = compoundtag.getCompound("CarvingMasks");

         for(String s1 : compoundtag5.getAllKeys()) {
            GenerationStep.Carving generationstep$carving = GenerationStep.Carving.valueOf(s1);
            protochunk1.setCarvingMask(generationstep$carving, BitSet.valueOf(compoundtag5.getByteArray(s1)));
         }

         return protochunk1;
      }
   }

   public static CompoundTag write(ServerLevel p_63455_, ChunkAccess p_63456_) {
      ChunkPos chunkpos = p_63456_.getPos();
      CompoundTag compoundtag = new CompoundTag();
      CompoundTag compoundtag1 = new CompoundTag();
      compoundtag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      compoundtag.put("Level", compoundtag1);
      compoundtag1.putInt("xPos", chunkpos.x);
      compoundtag1.putInt("zPos", chunkpos.z);
      compoundtag1.putLong("LastUpdate", p_63455_.getGameTime());
      compoundtag1.putLong("InhabitedTime", p_63456_.getInhabitedTime());
      compoundtag1.putString("Status", p_63456_.getStatus().getName());
      UpgradeData upgradedata = p_63456_.getUpgradeData();
      if (!upgradedata.isEmpty()) {
         compoundtag1.put("UpgradeData", upgradedata.write());
      }

      LevelChunkSection[] alevelchunksection = p_63456_.getSections();
      ListTag listtag = new ListTag();
      LevelLightEngine levellightengine = p_63455_.getChunkSource().getLightEngine();
      boolean flag = p_63456_.isLightCorrect();

      for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
         int j = i;
         LevelChunkSection levelchunksection = Arrays.stream(alevelchunksection).filter((p_63453_) -> {
            return p_63453_ != null && SectionPos.blockToSectionCoord(p_63453_.bottomBlockY()) == j;
         }).findFirst().orElse(LevelChunk.EMPTY_SECTION);
         DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, j));
         DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, j));
         if (levelchunksection != LevelChunk.EMPTY_SECTION || datalayer != null || datalayer1 != null) {
            CompoundTag compoundtag2 = new CompoundTag();
            compoundtag2.putByte("Y", (byte)(j & 255));
            if (levelchunksection != LevelChunk.EMPTY_SECTION) {
               levelchunksection.getStates().write(compoundtag2, "Palette", "BlockStates");
            }

            if (datalayer != null && !datalayer.isEmpty()) {
               compoundtag2.putByteArray("BlockLight", datalayer.getData());
            }

            if (datalayer1 != null && !datalayer1.isEmpty()) {
               compoundtag2.putByteArray("SkyLight", datalayer1.getData());
            }

            listtag.add(compoundtag2);
         }
      }

      compoundtag1.put("Sections", listtag);
      if (flag) {
         compoundtag1.putBoolean("isLightOn", true);
      }

      ChunkBiomeContainer chunkbiomecontainer = p_63456_.getBiomes();
      if (chunkbiomecontainer != null) {
         compoundtag1.putIntArray("Biomes", chunkbiomecontainer.writeBiomes());
      }

      ListTag listtag1 = new ListTag();

      for(BlockPos blockpos : p_63456_.getBlockEntitiesPos()) {
         CompoundTag compoundtag3 = p_63456_.getBlockEntityNbtForSaving(blockpos);
         if (compoundtag3 != null) {
            listtag1.add(compoundtag3);
         }
      }

      compoundtag1.put("TileEntities", listtag1);
      if (p_63456_.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
         ProtoChunk protochunk = (ProtoChunk)p_63456_;
         ListTag listtag2 = new ListTag();
         listtag2.addAll(protochunk.getEntities());
         compoundtag1.put("Entities", listtag2);
         compoundtag1.put("Lights", packOffsets(protochunk.getPackedLights()));
         CompoundTag compoundtag4 = new CompoundTag();

         for(GenerationStep.Carving generationstep$carving : GenerationStep.Carving.values()) {
            BitSet bitset = protochunk.getCarvingMask(generationstep$carving);
            if (bitset != null) {
               compoundtag4.putByteArray(generationstep$carving.toString(), bitset.toByteArray());
            }
         }

         compoundtag1.put("CarvingMasks", compoundtag4);
      }

      TickList<Block> ticklist = p_63456_.getBlockTicks();
      if (ticklist instanceof ProtoTickList) {
         compoundtag1.put("ToBeTicked", ((ProtoTickList)ticklist).save());
      } else if (ticklist instanceof ChunkTickList) {
         compoundtag1.put("TileTicks", ((ChunkTickList)ticklist).save());
      } else {
         compoundtag1.put("TileTicks", p_63455_.getBlockTicks().save(chunkpos));
      }

      TickList<Fluid> ticklist1 = p_63456_.getLiquidTicks();
      if (ticklist1 instanceof ProtoTickList) {
         compoundtag1.put("LiquidsToBeTicked", ((ProtoTickList)ticklist1).save());
      } else if (ticklist1 instanceof ChunkTickList) {
         compoundtag1.put("LiquidTicks", ((ChunkTickList)ticklist1).save());
      } else {
         compoundtag1.put("LiquidTicks", p_63455_.getLiquidTicks().save(chunkpos));
      }

      compoundtag1.put("PostProcessing", packOffsets(p_63456_.getPostProcessing()));
      CompoundTag compoundtag5 = new CompoundTag();

      for(Entry<Heightmap.Types, Heightmap> entry : p_63456_.getHeightmaps()) {
         if (p_63456_.getStatus().heightmapsAfter().contains(entry.getKey())) {
            compoundtag5.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
         }
      }

      compoundtag1.put("Heightmaps", compoundtag5);
      compoundtag1.put("Structures", packStructureData(p_63455_, chunkpos, p_63456_.getAllStarts(), p_63456_.getAllReferences()));
      return compoundtag;
   }

   public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag p_63486_) {
      if (p_63486_ != null) {
         ChunkStatus chunkstatus = ChunkStatus.byName(p_63486_.getCompound("Level").getString("Status"));
         if (chunkstatus != null) {
            return chunkstatus.getChunkType();
         }
      }

      return ChunkStatus.ChunkType.PROTOCHUNK;
   }

   private static void postLoadChunk(ServerLevel p_156523_, CompoundTag p_156524_, LevelChunk p_156525_) {
      if (p_156524_.contains("Entities", 9)) {
         ListTag listtag = p_156524_.getList("Entities", 10);
         if (!listtag.isEmpty()) {
            p_156523_.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listtag, p_156523_));
         }
      }

      ListTag listtag1 = p_156524_.getList("TileEntities", 10);

      for(int i = 0; i < listtag1.size(); ++i) {
         CompoundTag compoundtag = listtag1.getCompound(i);
         boolean flag = compoundtag.getBoolean("keepPacked");
         if (flag) {
            p_156525_.setBlockEntityNbt(compoundtag);
         } else {
            BlockPos blockpos = new BlockPos(compoundtag.getInt("x"), compoundtag.getInt("y"), compoundtag.getInt("z"));
            BlockEntity blockentity = BlockEntity.loadStatic(blockpos, p_156525_.getBlockState(blockpos), compoundtag);
            if (blockentity != null) {
               p_156525_.setBlockEntity(blockentity);
            }
         }
      }

   }

   private static CompoundTag packStructureData(ServerLevel p_156514_, ChunkPos p_156515_, Map<StructureFeature<?>, StructureStart<?>> p_156516_, Map<StructureFeature<?>, LongSet> p_156517_) {
      CompoundTag compoundtag = new CompoundTag();
      CompoundTag compoundtag1 = new CompoundTag();

      for(Entry<StructureFeature<?>, StructureStart<?>> entry : p_156516_.entrySet()) {
         compoundtag1.put(entry.getKey().getFeatureName(), entry.getValue().createTag(p_156514_, p_156515_));
      }

      compoundtag.put("Starts", compoundtag1);
      CompoundTag compoundtag2 = new CompoundTag();

      for(Entry<StructureFeature<?>, LongSet> entry1 : p_156517_.entrySet()) {
         compoundtag2.put(entry1.getKey().getFeatureName(), new LongArrayTag(entry1.getValue()));
      }

      compoundtag.put("References", compoundtag2);
      return compoundtag;
   }

   private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(ServerLevel p_156519_, CompoundTag p_156520_, long p_156521_) {
      Map<StructureFeature<?>, StructureStart<?>> map = Maps.newHashMap();
      CompoundTag compoundtag = p_156520_.getCompound("Starts");

      for(String s : compoundtag.getAllKeys()) {
         String s1 = s.toLowerCase(Locale.ROOT);
         StructureFeature<?> structurefeature = StructureFeature.STRUCTURES_REGISTRY.get(s1);
         if (structurefeature == null) {
            LOGGER.error("Unknown structure start: {}", (Object)s1);
         } else {
            StructureStart<?> structurestart = StructureFeature.loadStaticStart(p_156519_, compoundtag.getCompound(s), p_156521_);
            if (structurestart != null) {
               map.put(structurefeature, structurestart);
            }
         }
      }

      return map;
   }

   private static Map<StructureFeature<?>, LongSet> unpackStructureReferences(ChunkPos p_63472_, CompoundTag p_63473_) {
      Map<StructureFeature<?>, LongSet> map = Maps.newHashMap();
      CompoundTag compoundtag = p_63473_.getCompound("References");

      for(String s : compoundtag.getAllKeys()) {
         String s1 = s.toLowerCase(Locale.ROOT);
         StructureFeature<?> structurefeature = StructureFeature.STRUCTURES_REGISTRY.get(s1);
         if (structurefeature == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", s1, p_63472_);
         } else {
            map.put(structurefeature, new LongOpenHashSet(Arrays.stream(compoundtag.getLongArray(s)).filter((p_156529_) -> {
               ChunkPos chunkpos = new ChunkPos(p_156529_);
               if (chunkpos.getChessboardDistance(p_63472_) > 8) {
                  LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", s1, chunkpos, p_63472_);
                  return false;
               } else {
                  return true;
               }
            }).toArray()));
         }
      }

      return map;
   }

   public static ListTag packOffsets(ShortList[] p_63491_) {
      ListTag listtag = new ListTag();

      for(ShortList shortlist : p_63491_) {
         ListTag listtag1 = new ListTag();
         if (shortlist != null) {
            for(Short oshort : shortlist) {
               listtag1.add(ShortTag.valueOf(oshort));
            }
         }

         listtag.add(listtag1);
      }

      return listtag;
   }
}